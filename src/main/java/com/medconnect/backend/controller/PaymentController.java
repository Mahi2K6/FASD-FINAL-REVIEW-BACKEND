package com.medconnect.backend.controller;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.Payment;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.dto.ProcessPaymentRequest;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public PaymentController(PaymentService paymentService, UserRepository userRepository) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/process")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> processPayment(@RequestBody ProcessPaymentRequest request, Principal principal) {
        System.out.println("Incoming Payment Request: " + request);
        try {
            User user = getUser(principal);
            Payment payment = paymentService.processPayment(user.getId(), request);

            if (payment.getStatus().name().equals("FAILED")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "reason", payment.getFailureReason() != null ? payment.getFailureReason() : "Payment Failed"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "payment", payment
            ));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "reason", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "reason", "An unexpected error occurred during payment processing"
            ));
        }
    }

    @GetMapping("/history/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Payment>> getPaymentHistoryByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(userId));
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Payment>> getPaymentHistory(Principal principal) {
        User user = getUser(principal);
        return ResponseEntity.ok(paymentService.getPaymentHistory(user.getId()));
    }

    @GetMapping("/summary/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, BigDecimal>> getPaymentSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentSummary(userId));
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, BigDecimal>> getPaymentSummaryForCurrentUser(Principal principal) {
        User user = getUser(principal);
        return ResponseEntity.ok(paymentService.getPaymentSummary(user.getId()));
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Payment> getPaymentDetails(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentDetails(paymentId));
    }

    private User getUser(Principal principal) {
        return userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
