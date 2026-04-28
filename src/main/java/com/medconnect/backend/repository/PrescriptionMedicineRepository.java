package com.medconnect.backend.repository;

import com.medconnect.backend.model.PrescriptionMedicine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionMedicineRepository extends JpaRepository<PrescriptionMedicine, Long> {

    List<PrescriptionMedicine> findByPrescription_Id(Long prescriptionId);

    void deleteByPrescription_Id(Long prescriptionId);
}
