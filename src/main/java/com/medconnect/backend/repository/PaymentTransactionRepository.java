package com.medconnect.backend.repository;

import com.medconnect.backend.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<PaymentTransaction> findByAppointmentId(Long appointmentId);
}
