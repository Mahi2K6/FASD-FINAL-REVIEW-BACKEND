package com.medconnect.backend.service;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.PaymentMethod;
import com.medconnect.backend.repository.PaymentMethodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
    }

    @Transactional
    public PaymentMethod savePaymentMethod(Long userId, PaymentMethod paymentMethod) {
        paymentMethod.setUserId(userId);
        
        // If this is the first payment method, or it's marked as default, make it default
        List<PaymentMethod> existingMethods = paymentMethodRepository.findByUserId(userId);
        if (existingMethods.isEmpty() || Boolean.TRUE.equals(paymentMethod.getIsDefault())) {
            setDefault(userId, null); // Unset others
            paymentMethod.setIsDefault(true);
        } else {
            paymentMethod.setIsDefault(false);
        }

        return paymentMethodRepository.save(paymentMethod);
    }

    public List<PaymentMethod> getUserPaymentMethods(Long userId) {
        return paymentMethodRepository.findByUserId(userId);
    }

    @Transactional
    public void deletePaymentMethod(Long id, Long userId) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        if (!method.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        paymentMethodRepository.delete(method);
        
        // If we deleted the default, set a new default if there are others
        if (Boolean.TRUE.equals(method.getIsDefault())) {
            List<PaymentMethod> remaining = paymentMethodRepository.findByUserId(userId);
            if (!remaining.isEmpty()) {
                PaymentMethod newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                paymentMethodRepository.save(newDefault);
            }
        }
    }

    @Transactional
    public PaymentMethod setDefault(Long userId, Long methodId) {
        List<PaymentMethod> methods = paymentMethodRepository.findByUserId(userId);
        PaymentMethod newDefault = null;

        for (PaymentMethod method : methods) {
            if (method.getId().equals(methodId)) {
                method.setIsDefault(true);
                newDefault = method;
            } else {
                method.setIsDefault(false);
            }
        }
        
        if (!methods.isEmpty()) {
            paymentMethodRepository.saveAll(methods);
        }

        if (methodId != null && newDefault == null) {
            throw new ResourceNotFoundException("Payment method not found");
        }

        return newDefault;
    }
}
