package com.medconnect.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.medconnect.backend.model.Appointment;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Find all appointments for a specific doctor
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByDoctorIdOrderByAppointmentDateDescIdDesc(Long doctorId);
    
    // Find all appointments for a specific patient
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByPatientIdOrderByAppointmentDateDescIdDesc(Long patientId);

    boolean existsBySlotId(Long slotId);

    void deleteByPatientId(Long patientId);

    void deleteByDoctorId(Long doctorId);
    
}