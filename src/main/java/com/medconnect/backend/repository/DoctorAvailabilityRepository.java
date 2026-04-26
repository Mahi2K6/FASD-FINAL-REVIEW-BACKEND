package com.medconnect.backend.repository;

import com.medconnect.backend.model.DoctorAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    boolean existsByDoctorIdAndSlotDateAndStartTime(Long doctorId, LocalDate slotDate, LocalTime startTime);

    List<DoctorAvailability> findByDoctorIdAndSlotDateOrderByStartTimeAsc(Long doctorId, LocalDate slotDate);

    @Query("SELECT s FROM DoctorAvailability s WHERE s.doctorId = :doctorId AND s.slotDate = :slotDate AND s.booked = false ORDER BY s.startTime ASC")
    List<DoctorAvailability> findAvailableSlots(@Param("doctorId") Long doctorId, @Param("slotDate") LocalDate slotDate);

    @Query("SELECT s FROM DoctorAvailability s WHERE s.doctorId = :doctorId AND s.booked = false AND (s.slotDate > :today OR (s.slotDate = :today AND s.startTime > :now)) ORDER BY s.slotDate ASC, s.startTime ASC")
    List<DoctorAvailability> findUpcomingAvailableSlots(@Param("doctorId") Long doctorId, @Param("today") LocalDate today, @Param("now") LocalTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DoctorAvailability s WHERE s.id = :id")
    Optional<DoctorAvailability> findByIdForUpdate(@Param("id") Long id);
}
