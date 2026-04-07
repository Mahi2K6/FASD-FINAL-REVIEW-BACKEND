package com.medconnect.backend.service.impl;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.exception.SlotAlreadyBookedException;
import com.medconnect.backend.model.Appointment;
import com.medconnect.backend.model.DoctorAvailability;
import com.medconnect.backend.model.PaymentStatus;
import com.medconnect.backend.model.User;
import com.medconnect.backend.repository.AppointmentRepository;
import com.medconnect.backend.repository.DoctorAvailabilityRepository;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.AppointmentService;
import com.medconnect.backend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;

    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            DoctorAvailabilityRepository doctorAvailabilityRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Appointment book(Appointment appointment) {
        System.out.println("Booking request slotId = " + appointment.getSlotId());
        if (appointment.getSlotId() == null) {
            throw new RuntimeException("slotId is required");
        }
        return bookWithSlot(appointment);
    }

    private Appointment bookWithSlot(Appointment appointment) {
        Long slotId = appointment.getSlotId();
        DoctorAvailability slot = doctorAvailabilityRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new RuntimeException("Invalid slot"));

        if (slot.isBooked()) {
            throw new RuntimeException("Slot already booked");
        }
        if (appointmentRepository.existsBySlotId(slotId)) {
            throw new SlotAlreadyBookedException("This slot has already been booked.");
        }
        ZoneId zone = ZoneId.systemDefault();
        if (isSlotExpired(slot, zone)) {
            throw new RuntimeException("Slot is no longer available");
        }
        if (appointment.getDoctorId() != null && !appointment.getDoctorId().equals(slot.getDoctorId())) {
            throw new RuntimeException("doctorId does not match slot");
        }
        if (slot.getStartTime() == null || slot.getEndTime() == null) {
            throw new RuntimeException("Invalid slot data");
        }

        // Mark the slot first inside the same transaction to reduce race windows.
        slot.setBooked(true);
        doctorAvailabilityRepository.saveAndFlush(slot);

        appointment.setSlotId(slotId);
        appointment.setDoctorId(slot.getDoctorId());
        appointment.setAppointmentDate(Date.from(slot.getSlotDate().atStartOfDay(zone).toInstant()));
        appointment.setStartTime(formatSlotTime(slot.getStartTime()));
        appointment.setEndTime(formatSlotTime(slot.getEndTime()));
        appointment.setProblemDescription(appointment.getProblemDescription());
        appointment.setStatus("PENDING");
        if (appointment.getPaymentStatus() == null) {
            appointment.setPaymentStatus(PaymentStatus.PENDING);
        }

        Appointment saved = appointmentRepository.saveAndFlush(appointment);
        notifyDoctorNewAppointment(saved);
        return saved;
    }

    private void notifyDoctorNewAppointment(Appointment saved) {
        String patientName = "a patient";
        if (saved.getPatientId() != null) {
            patientName = userRepository.findById(saved.getPatientId())
                    .map(User::getName)
                    .orElse(patientName);
        }
        notificationService.createNotification(
                saved.getDoctorId(),
                "New Appointment",
                "You have a new appointment request from " + patientName,
                "APPOINTMENT"
        );
    }

    private static String formatSlotTime(LocalTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    private static boolean isSlotExpired(DoctorAvailability slot, ZoneId zone) {
        LocalDate today = LocalDate.now(zone);
        LocalTime now = LocalTime.now(zone);
        if (slot.getSlotDate().isBefore(today)) {
            return true;
        }
        if (slot.getSlotDate().isAfter(today)) {
            return false;
        }
        return slot.getStartTime().isBefore(now);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByDoctorId(Long doctorId) {
        return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescIdDesc(doctorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByPatientId(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDescIdDesc(patientId);
    }

    @Override
    @Transactional
    public Appointment updateStatus(Long id, String status) {
        Appointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
        a.setStatus(status);
        return appointmentRepository.save(a);
    }

    @Override
    @Transactional
    public Appointment saveCallSummary(Long id, String summary) {
        Appointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
        a.setCallSummary(summary);
        a.setStatus("COMPLETED");
        return appointmentRepository.save(a);
    }

    @Override
    @Transactional
    public Appointment markPaymentPaid(Long appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));
        a.setPaymentStatus(PaymentStatus.PAID);
        return appointmentRepository.save(a);
    }
}
