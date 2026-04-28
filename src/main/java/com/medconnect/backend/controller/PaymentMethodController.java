package com.medconnect.backend.controller;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.PaymentMethod;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.dto.SavePaymentMethodRequest;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.PaymentMethodService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@CrossOrigin(origins = "*")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;
    private final UserRepository userRepository;

    public PaymentMethodController(PaymentMethodService paymentMethodService, UserRepository userRepository) {
        this.paymentMethodService = paymentMethodService;
        this.userRepository = userRepository;
    }

    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentMethod> savePaymentMethod(@RequestBody SavePaymentMethodRequest request, Principal principal) {
        User user = getUser(principal);
        PaymentMethod method = new PaymentMethod();
        method.setCardLast4(request.getCardLast4());
        method.setCardBrand(request.getCardBrand());
        method.setExpiryMonth(request.getExpiryMonth());
        method.setExpiryYear(request.getExpiryYear());
        method.setTokenReference(request.getTokenReference());
        method.setIsDefault(request.getIsDefault());

        return ResponseEntity.ok(paymentMethodService.savePaymentMethod(user.getId(), method));
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentMethod>> getUserPaymentMethods(@PathVariable Long id) {
        return ResponseEntity.ok(paymentMethodService.getUserPaymentMethods(id));
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentMethod>> getCurrentUserPaymentMethods(Principal principal) {
        User user = getUser(principal);
        return ResponseEntity.ok(paymentMethodService.getUserPaymentMethods(user.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable Long id, Principal principal) {
        User user = getUser(principal);
        paymentMethodService.deletePaymentMethod(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/default/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentMethod> setDefaultPaymentMethod(@PathVariable Long id, Principal principal) {
        User user = getUser(principal);
        return ResponseEntity.ok(paymentMethodService.setDefault(user.getId(), id));
    }

    private User getUser(Principal principal) {
        return userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
