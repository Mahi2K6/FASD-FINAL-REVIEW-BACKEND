package com.medconnect.backend.repository;

import com.medconnect.backend.model.PharmacyOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PharmacyOrderRepository extends JpaRepository<PharmacyOrder, Long> {

    List<PharmacyOrder> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<PharmacyOrder> findByPharmacistIdOrderByCreatedAtDesc(Long pharmacistId);

    List<PharmacyOrder> findByPrescriptionId(Long prescriptionId);
}
