package com.medconnect.backend.service;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.Payment;
import com.medconnect.backend.model.PaymentStatus;
import com.medconnect.backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medconnect.backend.model.dto.ProcessPaymentRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment processPayment(Long userId, ProcessPaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAppointmentId(request.getAppointmentId());
        payment.setOrderId(request.getOrderId());
        
        // Validation logic for specific methods
        if ("UPI".equalsIgnoreCase(request.getPaymentMethod())) {
            if (request.getUpiId() == null || !request.getUpiId().contains("@")) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Invalid UPI ID");
                return paymentRepository.save(payment);
            }
        } else if ("CARD".equalsIgnoreCase(request.getPaymentMethod())) {
            if (request.getCardNumber() == null || request.getCardNumber().length() < 16) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Invalid Card Number");
                return paymentRepository.save(payment);
            }
            if (request.getCvv() == null || request.getCvv().length() < 3) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Invalid CVV");
                return paymentRepository.save(payment);
            }
        }

        // Simulate payment failure reasons based on amount
        if (request.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("insufficient balance");
        } else if (request.getAmount().compareTo(new BigDecimal("5000")) == 0) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("card expired");
        } else if (request.getAmount().compareTo(new BigDecimal("999")) == 0) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("bank timeout");
        } else {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(UUID.randomUUID().toString());
        }

        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentHistory(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Map<String, BigDecimal> getPaymentSummary(Long userId) {
        List<Payment> payments = paymentRepository.findByUserId(userId);
        
        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal refunds = BigDecimal.ZERO;
        BigDecimal pending = BigDecimal.ZERO;
        BigDecimal failed = BigDecimal.ZERO;

        for (Payment p : payments) {
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                totalSpent = totalSpent.add(p.getAmount());
            } else if (p.getStatus() == PaymentStatus.REFUNDED) {
                refunds = refunds.add(p.getAmount());
            } else if (p.getStatus() == PaymentStatus.PENDING) {
                pending = pending.add(p.getAmount());
            } else if (p.getStatus() == PaymentStatus.FAILED) {
                failed = failed.add(p.getAmount());
            }
        }

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalSpent", totalSpent);
        summary.put("refunds", refunds);
        summary.put("pending", pending);
        summary.put("failedPayments", failed);

        return summary;
    }

    public Payment getPaymentDetails(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));
    }
}
