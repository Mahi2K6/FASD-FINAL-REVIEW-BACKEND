package com.medconnect.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medconnect.backend.model.AiConversationLog;
import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.model.DoctorAvailability;
import com.medconnect.backend.model.PaymentStatus;
import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.UserStatus;
import com.medconnect.backend.model.dto.AgentResponse;
import com.medconnect.backend.model.dto.DoctorDTO;
import com.medconnect.backend.repository.AiConversationLogRepository;
import com.medconnect.backend.repository.AppointmentRepository;
import com.medconnect.backend.repository.DoctorAvailabilityRepository;
import com.medconnect.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiAgentService.class);
    private static final String DEFAULT_SPECIALTY = "General Medicine";
    private static final DateTimeFormatter SLOT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserRepository userRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityService doctorAvailabilityService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String geminiApiKey;
    private final String geminiModel;
    private final AiConversationLogRepository aiConversationLogRepository;

    public AiAgentService(
            UserRepository userRepository,
            DoctorAvailabilityRepository doctorAvailabilityRepository,
            AppointmentRepository appointmentRepository,
            DoctorAvailabilityService doctorAvailabilityService,
            ObjectMapper objectMapper,
            AiConversationLogRepository aiConversationLogRepository,
            @Value("${app.ai.gemini-api-key:}") String geminiApiKey,
            @Value("${app.ai.gemini-model:gemini-2.5-flash}") String geminiModel
    ) {
        this.userRepository = userRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorAvailabilityService = doctorAvailabilityService;
        this.objectMapper = objectMapper;
        this.aiConversationLogRepository = aiConversationLogRepository;
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Transactional
    public AgentResponse processBookingRequest(String requestText, String userEmail) {
        User patient = userRepository.findByEmail(userEmail.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        Analysis analysis = extractAnalysis(requestText, userEmail);
        String specialty = safeSpecialty(analysis.specialty);
        String normalizedSpecialty = normalizeSpecialty(specialty);
        String priority = safePriority(analysis.priority);

        log.info("AI specialty returned: {}", specialty);
        log.info("Normalized specialty: {}", normalizedSpecialty);
        log.info("Repository query value: {}", normalizedSpecialty);
        List<User> allUsers = userRepository.findAll();
        List<User> allDoctors = allUsers.stream()
                .filter(user -> user.getRole() == Role.DOCTOR)
                .toList();
        log.info("Total users fetched from DB: {}", allUsers.size());
        log.info("Total doctors fetched from DB: {}", allDoctors.size());
        log.info("Doctor specialization values found in DB: {}", allDoctors.stream().map(User::getSpecialization).toList());
        log.info("Doctor role values found in DB: {}", allDoctors.stream().map(User::getRole).toList());
        log.info("Doctor status values found in DB: {}", allDoctors.stream().map(User::getStatus).toList());
        for (User doctor : allDoctors) {
            log.info("Doctor DB row -> id={} name={} role={} specialization={} status={}",
                    doctor.getId(), doctor.getName(), doctor.getRole(), doctor.getSpecialization(), doctor.getStatus());
        }

        List<User> doctors = findDoctorsBySpecialty(normalizedSpecialty, allDoctors);
        log.info("Total doctors after filtering: {}", doctors.size());

        if (doctors.isEmpty()) {
            String normalizedFallback = normalizeSpecialty(DEFAULT_SPECIALTY);
            log.info("Repository query value (fallback): {}", normalizedFallback);
            doctors = findDoctorsBySpecialty(normalizedFallback, allDoctors);
            specialty = normalizedFallback;
            log.info("Total doctors after filtering with General fallback: {}", doctors.size());
        } else {
            specialty = normalizedSpecialty;
        }

        AgentResponse response = new AgentResponse();
        response.setIntent(defaultIfBlank(analysis.intent, "book_appointment"));
        response.setSymptom(defaultIfBlank(analysis.symptom, "unspecified"));
        response.setPriority(priority);
        response.setSpecialty(specialty);
        response.setReasoning(defaultIfBlank(
                analysis.reasoning,
                "Specialty and urgency were selected from your symptoms and booking intent."
        ));

        if (doctors.isEmpty()) {
            response.setReasoning(response.getReasoning() + " No active doctors are currently available for this specialty.");
            return response;
        }

        List<DoctorCandidate> ranked = rankDoctors(doctors, priority);
        if (ranked.isEmpty()) {
            log.info("No slots available for matched doctors, attempting temporary draft slot fallback.");
            User fallbackDoctor = doctors.get(0);
            DoctorAvailability fallbackSlot = createTemporaryDraftSlot(fallbackDoctor.getId());
            if (fallbackSlot == null) {
                response.setReasoning(response.getReasoning() + " No open slots were found right now.");
                return response;
            }
            ranked = List.of(new DoctorCandidate(fallbackDoctor, fallbackSlot, deriveRating(fallbackDoctor)));
        }

        DoctorCandidate selected = ranked.get(0);
        log.info("Chosen doctor: {} | specialization: {}", selected.doctor().getName(), selected.doctor().getSpecialization());
        log.info("Available slots found for chosen doctor: {}", doctorAvailabilityRepository
                .findUpcomingAvailableSlots(selected.doctor().getId(), LocalDate.now(), LocalTime.now())
                .size());

        DoctorAvailability lockedSlot = doctorAvailabilityRepository.findByIdForUpdate(selected.slot().getId())
                .orElse(null);
        if (lockedSlot == null || lockedSlot.isBooked()) {
            response.setReasoning(response.getReasoning() + " Selected slot became unavailable. Please try again.");
            return response;
        }

        lockedSlot.setBooked(true);
        doctorAvailabilityRepository.saveAndFlush(lockedSlot);

        log.info("Appointment creation attempt: patientId={} doctorId={} slotId={}",
                patient.getId(), selected.doctor().getId(), lockedSlot.getId());
        Appointment appointment = new Appointment();
        appointment.setPatientId(patient.getId());
        appointment.setDoctorId(selected.doctor().getId());
        appointment.setSlotId(lockedSlot.getId());
        appointment.setAppointmentDate(lockedSlot.getSlotDate());
        appointment.setStartTime(lockedSlot.getStartTime());
        appointment.setEndTime(lockedSlot.getEndTime());
        appointment.setProblemDescription("AI Agent: " + requestText);
        appointment.setStatus("PENDING_PAYMENT");
        appointment.setPaymentStatus(PaymentStatus.PENDING);
        appointment.setAmount(BigDecimal.ZERO);

        Appointment saved = appointmentRepository.saveAndFlush(appointment);

        response.setDoctor(toDoctorDto(selected.doctor(), selected.rating()));
        response.setSlot(lockedSlot.getSlotDate().atTime(lockedSlot.getStartTime()).format(SLOT_FORMAT));
        response.setAppointmentId(saved.getId().toString());
        response.setCheckoutUrl("/checkout/" + saved.getId());
        return response;
    }

    private List<DoctorCandidate> rankDoctors(List<User> doctors, String priority) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDate horizon = today.plusDays(14);
        List<DoctorCandidate> candidates = new ArrayList<>();

        for (User doctor : doctors) {
            for (LocalDate date = today; !date.isAfter(horizon); date = date.plusDays(1)) {
                doctorAvailabilityService.generateSlots(doctor.getId(), date);
            }
            List<DoctorAvailability> slots = doctorAvailabilityRepository.findUpcomingAvailableSlots(doctor.getId(), today, now);
            if (slots.isEmpty()) {
                continue;
            }
            DoctorAvailability earliestSlot = slots.get(0);
            double rating = deriveRating(doctor);
            candidates.add(new DoctorCandidate(doctor, earliestSlot, rating));
        }

        Comparator<DoctorCandidate> fastestComparator = Comparator
                .comparing((DoctorCandidate c) -> c.slot().getSlotDate())
                .thenComparing(c -> c.slot().getStartTime())
                .thenComparing((DoctorCandidate c) -> -c.rating());

        Comparator<DoctorCandidate> highestRatedComparator = Comparator
                .comparing((DoctorCandidate c) -> -c.rating())
                .thenComparing(c -> c.slot().getSlotDate())
                .thenComparing(c -> c.slot().getStartTime());

        Comparator<DoctorCandidate> balancedComparator = Comparator
                .comparing((DoctorCandidate c) -> c.slot().getSlotDate())
                .thenComparing(c -> c.slot().getStartTime())
                .thenComparing((DoctorCandidate c) -> -c.rating());

        Comparator<DoctorCandidate> comparator;
        if ("highest_rated".equals(priority)) {
            comparator = highestRatedComparator;
        } else if ("fastest".equals(priority)) {
            comparator = fastestComparator;
        } else {
            comparator = balancedComparator;
        }

        return candidates.stream()
                .sorted(comparator)
                .limit(5)
                .toList();
    }

    private Analysis extractAnalysis(String requestText, String userEmail) {
        AiConversationLog logEntry = new AiConversationLog();
        logEntry.setUserEmail(userEmail);
        logEntry.setRequestText(requestText);

        // If Gemini API key is missing, use fallback immediately
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return saveFallbackAndReturn(logEntry, requestText, "Gemini key missing, fallback used.");
        }

        String prompt = """
                You are a healthcare appointment booking AI.
                Extract:
                - symptom
                - intent
                - priority
                - specialty
                - reasoning
                Rules:
                - Return ONLY JSON.
                - intent must be snake_case, preferably book_appointment.
                - priority must be one of: fastest, highest_rated, normal.
                - specialty should be a common medical specialty.
                
                User request:
                %s
                """.formatted(requestText);

        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String payload = objectMapper.writeValueAsString(buildGeminiPayload(prompt));
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent?key=" + geminiApiKey))
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(15))
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 != 2) {
                    log.warn("Gemini call failed with status {} body {}", response.statusCode(), response.body());
                    if (response.statusCode() == 429 && attempt < maxRetries) {
                        try { Thread.sleep(1000L * attempt); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        continue;
                    }
                    return saveFallbackAndReturn(logEntry, requestText, "AI service unavailable (status " + response.statusCode() + ").");
                }

                Analysis analysis = parseGeminiResponse(response.body(), requestText);
                
                logEntry.setResponseText(response.body());
                logEntry.setIsFallback(false);
                aiConversationLogRepository.save(logEntry);
                
                return analysis;
            } catch (Exception ex) {
                log.warn("Gemini processing failed on attempt " + attempt, ex);
                if (attempt < maxRetries) {
                    try { Thread.sleep(1000L * attempt); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    continue;
                }
                logEntry.setErrorMessage(ex.getMessage());
                return saveFallbackAndReturn(logEntry, requestText, "AI processing failed after retries.");
            }
        }
        return saveFallbackAndReturn(logEntry, requestText, "AI processing exhausted retries.");
    }

    private Analysis saveFallbackAndReturn(AiConversationLog logEntry, String requestText, String reason) {
        Analysis analysis = fallbackAnalysis(requestText, reason);
        logEntry.setIsFallback(true);
        logEntry.setErrorMessage(reason);
        try {
            logEntry.setResponseText(objectMapper.writeValueAsString(analysis));
        } catch (Exception ignored) {}
        aiConversationLogRepository.save(logEntry);
        return analysis;
    }

    private Map<String, Object> buildGeminiPayload(String prompt) {
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("responseMimeType", "application/json");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("contents", List.of(content));
        payload.put("generationConfig", generationConfig);
        return payload;
    }

    private Analysis parseGeminiResponse(String rawBody, String requestText) throws IOException {
        JsonNode root = objectMapper.readTree(rawBody);
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            return fallbackAnalysis(requestText, "AI empty response, fallback used.");
        }

        String cleaned = cleanJson(textNode.asText());
        JsonNode json = objectMapper.readTree(cleaned);

        Analysis analysis = new Analysis();
        analysis.symptom = json.path("symptom").asText(null);
        analysis.intent = json.path("intent").asText(null);
        analysis.priority = json.path("priority").asText(null);
        analysis.specialty = json.path("specialty").asText(null);
        analysis.reasoning = json.path("reasoning").asText(null);

        if (analysis.specialty == null || analysis.specialty.isBlank()) {
            analysis.specialty = DEFAULT_SPECIALTY;
        }
        return analysis;
    }

    private String cleanJson(String text) {
        String cleaned = text.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
            cleaned = cleaned.replaceFirst("\\s*```$", "");
        }
        return cleaned.trim();
    }

    private Analysis fallbackAnalysis(String requestText, String reason) {
        Analysis analysis = new Analysis();
        analysis.symptom = inferSymptom(requestText);
        analysis.intent = "book_appointment";
        analysis.priority = inferPriority(requestText);
        analysis.specialty = inferSpecialty(requestText);
        analysis.reasoning = reason;
        return analysis;
    }

    /**
     * Provides fallback advice when Gemini API is unavailable or quota exceeded.
     * Maps symptoms to appropriate doctor specialties.
     * 
     * @param input User's symptom description
     * @return Friendly advice recommending a doctor type
     */
    private String fallbackAdvice(String input) {
        if (input == null || input.isBlank()) {
            return "I'm unable to process your request right now. Please try again later or consult with a healthcare professional.";
        }

        // Normalize input: lowercase, trim, remove punctuation
        String normalized = input.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[\\p{Punct}]", "");

        // Symptom to doctor mapping
        if (normalized.contains("fever") || normalized.contains("cold") || normalized.contains("flu")) {
            if (normalized.contains("child") || normalized.contains("baby") || normalized.contains("kid")) {
                return "Based on your symptoms, you may consult a Pediatrician.";
            }
            return "Based on your symptoms, you may consult a General Physician.";
        }
        
        if (normalized.contains("cough")) {
            return "Based on your symptoms, you may consult a Pulmonologist.";
        }
        
        if (normalized.contains("headache") || normalized.contains("migraine")) {
            return "Based on your symptoms, you may consult a Neurologist or General Physician.";
        }
        
        if (normalized.contains("skin") || normalized.contains("rash") || normalized.contains("itch") || normalized.contains("allergy")) {
            return "Based on your symptoms, you may consult a Dermatologist.";
        }
        
        if (normalized.contains("chest pain") || normalized.contains("heart") || normalized.contains("cardio") || normalized.contains("palpitation")) {
            return "Based on your symptoms, you may consult a Cardiologist.";
        }
        
        if (normalized.contains("stomach pain") || normalized.contains("abdominal pain") || normalized.contains("digest") || normalized.contains("nausea") || normalized.contains("vomiting")) {
            return "Based on your symptoms, you may consult a Gastroenterologist.";
        }
        
        if (normalized.contains("anxiety") || normalized.contains("stress") || normalized.contains("depression") || normalized.contains("mental")) {
            return "Based on your symptoms, you may consult a Psychiatrist.";
        }
        
        // Default fallback
        return "Based on your symptoms, you may consult a General Physician for initial evaluation.";
    }

    private String inferPriority(String requestText) {
        String lowered = requestText.toLowerCase(Locale.ROOT);
        if (lowered.contains("best") || lowered.contains("top")) {
            return "highest_rated";
        }
        if (lowered.contains("as soon as possible") || lowered.contains("urgent") || lowered.contains("fast") || lowered.contains("quick")) {
            return "fastest";
        }
        return "normal";
    }

    private String inferSpecialty(String requestText) {
        String lowered = requestText.toLowerCase(Locale.ROOT);
        if (lowered.contains("headache") || lowered.contains("migraine") || lowered.contains("neuro")) {
            return "Neurology";
        }
        if (lowered.contains("skin") || lowered.contains("rash") || lowered.contains("derma")) {
            return "Dermatology";
        }
        if (lowered.contains("heart") || lowered.contains("chest pain") || lowered.contains("cardio")) {
            return "Cardiology";
        }
        if (lowered.contains("fever") || lowered.contains("cold") || lowered.contains("flu")) {
            return "General Medicine";
        }
        return DEFAULT_SPECIALTY;
    }

    private String inferSymptom(String requestText) {
        String normalized = requestText == null ? "" : requestText.trim();
        return normalized.isBlank() ? "unspecified" : normalized;
    }

    private String safeSpecialty(String specialty) {
        return defaultIfBlank(specialty, DEFAULT_SPECIALTY);
    }

    private String normalizeSpecialty(String specialty) {
        String value = defaultIfBlank(specialty, DEFAULT_SPECIALTY).toLowerCase(Locale.ROOT).trim();
        return switch (value) {
            case "general medicine", "general physician", "physician", "general doctor" -> "General";
            case "family doctor", "family physician" -> "General";
            case "dermatology", "dermatologist" -> "Dermatology";
            case "neurology", "neurologist" -> "Neurology";
            case "cardiology", "cardiologist" -> "Cardiology";
            case "pediatrics", "paediatrics", "pediatrician", "paediatrician" -> "Pediatrics";
            case "orthopedics", "orthopaedics", "orthopedic", "orthopaedic" -> "Orthopedics";
            case "obstetrics and gynecology", "obstetrics & gynecology", "obgyn", "gynaecology", "gynecology" -> "Gynecology";
            default -> {
                if (value.contains("general")) {
                    yield "General";
                }
                if (value.contains("physician") || value.contains("family")) {
                    yield "General";
                }
                if (value.contains("derma")) {
                    yield "Dermatology";
                }
                if (value.contains("neuro")) {
                    yield "Neurology";
                }
                if (value.contains("cardio")) {
                    yield "Cardiology";
                }
                if (value.contains("pedia")) {
                    yield "Pediatrics";
                }
                if (value.contains("ortho")) {
                    yield "Orthopedics";
                }
                if (value.contains("gyn") || value.contains("obstet")) {
                    yield "Gynecology";
                }
                yield defaultIfBlank(specialty, DEFAULT_SPECIALTY);
            }
        };
    }

    private String safePriority(String priority) {
        String normalized = defaultIfBlank(priority, "normal").toLowerCase(Locale.ROOT);
        if ("fastest".equals(normalized) || "highest_rated".equals(normalized) || "normal".equals(normalized)) {
            return normalized;
        }
        return "normal";
    }

    private String defaultIfBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private List<User> findDoctorsBySpecialty(String specialty, List<User> allDoctors) {
        String queryValue = defaultIfBlank(specialty, "General");
        String normalizedQuery = normalizeSpecialty(queryValue);

        // Step 1: repository lookup (as requested), then tolerant in-memory check on returned rows.
        List<User> repoRows = userRepository.findBySpecializationContainingIgnoreCase(queryValue).stream()
                .filter(user -> user.getRole() == Role.DOCTOR)
                .toList();
        List<User> repoMatched = repoRows.stream()
                .filter(user -> matchesSpecialtyTolerant(user.getSpecialization(), normalizedQuery))
                .toList();
        if (!repoMatched.isEmpty()) {
            log.info("Doctor specialization values found in DB (repo match): {}",
                    repoMatched.stream().map(User::getSpecialization).toList());
            return repoMatched;
        }

        // Step 2: fallback strategy - fetch all doctors and perform tolerant Java filtering.
        List<User> manuallyFiltered = allDoctors.stream()
                .filter(user -> matchesSpecialtyTolerant(user.getSpecialization(), normalizedQuery))
                .toList();
        if (!manuallyFiltered.isEmpty()) {
            log.info("Doctor specialization values found in DB (manual match): {}",
                    manuallyFiltered.stream().map(User::getSpecialization).toList());
            return manuallyFiltered;
        }

        // Step 3: final safeguard fallback to General matching.
        String normalizedGeneral = normalizeSpecialty("General");
        List<User> manualAnyStatus = allDoctors.stream()
                .filter(user -> matchesSpecialtyTolerant(user.getSpecialization(), normalizedGeneral))
                .toList();
        log.info("Doctor specialization values found in DB (manual General fallback match): {}",
                manualAnyStatus.stream().map(User::getSpecialization).toList());
        return manualAnyStatus;
    }

    private boolean matchesSpecialtyTolerant(String doctorSpecialization, String normalizedTarget) {
        if (doctorSpecialization == null || doctorSpecialization.isBlank()) {
            return false;
        }
        String doctorNormalized = normalizeSpecialty(doctorSpecialization);
        String left = doctorNormalized.toLowerCase(Locale.ROOT).trim();
        String right = normalizedTarget.toLowerCase(Locale.ROOT).trim();

        return left.equals(right)
                || left.contains(right)
                || right.contains(left)
                || ("general medicine".equals(left) && "general".equals(right))
                || ("general".equals(left) && "general medicine".equals(right))
                || (left.contains("neuro") && right.contains("neuro"))
                || (left.contains("derma") && right.contains("derma"))
                || ((left.contains("gyne") || left.contains("gyn")) && (right.contains("gyne") || right.contains("gyn")))
                || (left.contains("pediat") && right.contains("pediat"));
    }

    private DoctorAvailability createTemporaryDraftSlot(Long doctorId) {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime endBoundary = LocalTime.of(18, 0);

        for (int dayOffset = 0; dayOffset < 30; dayOffset++) {
            LocalDate targetDate = date.plusDays(dayOffset);
            LocalTime cursor = start;
            while (cursor.isBefore(endBoundary)) {
                LocalTime slotEnd = cursor.plusMinutes(30);
                if (slotEnd.isAfter(endBoundary)) {
                    break;
                }
                boolean exists = doctorAvailabilityRepository.existsByDoctorIdAndSlotDateAndStartTime(doctorId, targetDate, cursor);
                if (!exists) {
                    DoctorAvailability slot = new DoctorAvailability();
                    slot.setDoctorId(doctorId);
                    slot.setSlotDate(targetDate);
                    slot.setStartTime(cursor);
                    slot.setEndTime(slotEnd);
                    slot.setBooked(false);
                    DoctorAvailability saved = doctorAvailabilityRepository.saveAndFlush(slot);
                    log.info("Temporary draft slot generated for doctorId={} at {} {}", doctorId, targetDate, cursor);
                    return saved;
                }
                cursor = slotEnd;
            }
        }
        log.warn("Unable to create temporary draft slot for doctorId={}", doctorId);
        return null;
    }

    private double deriveRating(User doctor) {
        Integer experience = doctor.getExperience();
        if (experience == null || experience <= 0) {
            return 3.5;
        }
        return Math.min(5.0, 3.0 + (experience / 10.0));
    }

    private DoctorDTO toDoctorDto(User doctor, double rating) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setName(doctor.getName());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setExperience(doctor.getExperience());
        dto.setRating(rating);
        return dto;
    }

    private String formatTime(LocalTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    private static class Analysis {
        private String symptom;
        private String intent;
        private String priority;
        private String specialty;
        private String reasoning;
    }

    private record DoctorCandidate(User doctor, DoctorAvailability slot, double rating) {
    }
}