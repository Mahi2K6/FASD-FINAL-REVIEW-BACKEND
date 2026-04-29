package com.medconnect.backend.service.impl;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.Prescription;
import com.medconnect.backend.model.PrescriptionMedicine;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.dto.PrescriptionCreateRequest;
import com.medconnect.backend.model.dto.PrescriptionResponse;
import com.medconnect.backend.repository.PrescriptionMedicineRepository;
import com.medconnect.backend.repository.PrescriptionRepository;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.NotificationService;
import com.medconnect.backend.service.PrescriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMedicineRepository prescriptionMedicineRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final com.medconnect.backend.repository.AppointmentRepository appointmentRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public PrescriptionServiceImpl(
            PrescriptionRepository prescriptionRepository,
            PrescriptionMedicineRepository prescriptionMedicineRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            com.medconnect.backend.repository.AppointmentRepository appointmentRepository,
            org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate
    ) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionMedicineRepository = prescriptionMedicineRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.appointmentRepository = appointmentRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private User validateUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Unauthorized user"));
    }

    @Override
    @Transactional
    public Prescription add(Prescription prescription, String email) {
        System.out.println("Saving Prescription...");
        System.out.println("Appointment ID: " + prescription.getAppointmentId());

        User user = validateUser(email);

        if (prescription.getAppointmentId() == null || prescription.getPatientId() == null || 
            prescription.getDoctorId() == null || prescription.getDiagnosis() == null || 
            prescription.getDiagnosis().isBlank() || prescription.getMedicines() == null || 
            prescription.getMedicines().isEmpty()) {
            throw new IllegalArgumentException("Missing required fields: appointmentId, patientId, doctorId, diagnosis, or medicines");
        }

        if (prescriptionRepository.existsByAppointmentId(prescription.getAppointmentId())) {
            throw new IllegalArgumentException("Prescription already exists for this appointment");
        }

        com.medconnect.backend.model.Appointment appointment = appointmentRepository.findById(prescription.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
                
        if (!"COMPLETED".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalArgumentException("Consultation must be completed before issuing a prescription.");
        }

        if (!appointment.getDoctorId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Only the assigned doctor can issue a prescription for this appointment");
        }

        prescription.setStatus("ACTIVE");
        if (prescription.getMedicines() != null) {
            for (PrescriptionMedicine med : prescription.getMedicines()) {
                // Bi-directional constraint handled by JPA mappedBy
                med.setPrescription(prescription);
            }
        }
        
        Prescription saved;
        try {
            saved = prescriptionRepository.save(prescription);
        } catch (Exception ex) {
            System.err.println("Database save failed: " + ex.getMessage());
            throw new RuntimeException("Database save failed", ex);
        }
        
        System.out.println("Prescription created for appointment: " + appointment.getId());
        
        // Real-time socket sync
        messagingTemplate.convertAndSend("/topic/user-" + saved.getPatientId(), saved);

        // Notify patient
        if (saved.getPatientId() != null) {
            String doctorName = user.getName();
            notificationService.createNotification(
                    saved.getPatientId(),
                    "New Prescription",
                    doctorName + " has issued a new prescription for you.",
                    "PRESCRIPTION"
            );
        }
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prescription> findByPatientId(Long patientId, String email) {
        User user = validateUser(email);
        if (!user.getId().equals(patientId) && !"DOCTOR".equals(user.getRole())) {
            throw new org.springframework.security.access.AccessDeniedException("You cannot access another patient's prescriptions");
        }
        return prescriptionRepository.findByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prescription> findByDoctorId(Long doctorId, String email) {
        User user = validateUser(email);
        if (!user.getId().equals(doctorId)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only access your own prescriptions");
        }
        return prescriptionRepository.findByDoctorId(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Prescription findByAppointmentId(Long appointmentId, String email) {
        User user = validateUser(email);
        Prescription p = prescriptionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found for appointment: " + appointmentId));
        if (!p.getPatientId().equals(user.getId()) && !p.getDoctorId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this prescription");
        }
        return p;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Prescription> findPending() {
        return prescriptionRepository.findByStatus("ACTIVE");
    }

    @Override
    @Transactional
    public Prescription dispense(Long id) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + id));
        p.setStatus("COMPLETED");
        Prescription saved = prescriptionRepository.save(p);
        notificationService.createNotification(
                saved.getPatientId(),
                "Prescription Ready",
                "Your prescription has been dispensed.",
                "PRESCRIPTION"
        );
        return saved;
    }

    @Override
    @Transactional
    public Prescription update(Long id, Prescription prescription) {
        Prescription existing = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found: " + id));
        existing.setPatientId(prescription.getPatientId());
        existing.setDoctorId(prescription.getDoctorId());
        existing.setAppointmentId(prescription.getAppointmentId());
        
        existing.getMedicines().clear();
        if (prescription.getMedicines() != null) {
            existing.getMedicines().addAll(prescription.getMedicines());
        }
        
        existing.setStatus(prescription.getStatus());
        existing.setDiagnosis(prescription.getDiagnosis());
        existing.setNotes(prescription.getNotes());
        existing.setFollowUpRecommendation(prescription.getFollowUpRecommendation());
        return prescriptionRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        prescriptionRepository.deleteById(id);
    }

    // Unused Marketplace Extensions removed as they were DTO dependent.
    @Override
    public PrescriptionResponse createWithMedicines(PrescriptionCreateRequest request) { return null; }

    @Override
    public PrescriptionResponse getLatestByPatientId(Long patientId) { return null; }

    @Override
    public List<PrescriptionResponse> getHistoryByPatientId(Long patientId) { return List.of(); }
}