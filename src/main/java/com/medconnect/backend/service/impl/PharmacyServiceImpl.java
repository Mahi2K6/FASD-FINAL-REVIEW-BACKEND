package com.medconnect.backend.service.impl;

import com.medconnect.backend.model.PharmacyOrder;
import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.UserStatus;
import com.medconnect.backend.model.dto.PharmacyOrderRequest;
import com.medconnect.backend.model.dto.PharmacyResponse;
import com.medconnect.backend.repository.PharmacyOrderRepository;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.PharmacyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PharmacyServiceImpl implements PharmacyService {

    private final PharmacyOrderRepository pharmacyOrderRepository;
    private final UserRepository userRepository;

    private static final String[] STORE_NAMES = {
            "CityCare Pharmacy", "MedPlus Health Hub", "Apollo Pharmacy Express",
            "LifeLine Meds", "GreenCross Pharmacy", "WellCure Drugs", "HealthFirst Pharmacy"
    };

    private static final String[] DELIVERY_TIMES = {
            "15 mins", "20 mins", "25 mins", "30 mins", "45 mins"
    };

    public PharmacyServiceImpl(PharmacyOrderRepository pharmacyOrderRepository, UserRepository userRepository) {
        this.pharmacyOrderRepository = pharmacyOrderRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyResponse> getAvailablePharmacies() {
        // Find all active pharmacists from the user table
        List<User> pharmacists = userRepository.findByRoleAndStatus(Role.PHARMACIST, UserStatus.ACTIVE);
        List<PharmacyResponse> result = new ArrayList<>();

        Random random = new Random();

        if (pharmacists != null && !pharmacists.isEmpty()) {
            for (int i = 0; i < pharmacists.size(); i++) {
                User pharmacist = pharmacists.get(i);
                String storeName = i < STORE_NAMES.length ? STORE_NAMES[i] : "Pharmacy #" + (i + 1);
                String deliveryTime = DELIVERY_TIMES[random.nextInt(DELIVERY_TIMES.length)];
                double rating = 3.5 + (random.nextDouble() * 1.5); // 3.5 to 5.0
                rating = Math.round(rating * 10.0) / 10.0;
                BigDecimal estimatedCost = BigDecimal.valueOf(200 + random.nextInt(500));

                result.add(new PharmacyResponse(
                        pharmacist.getId(), storeName, pharmacist.getName(),
                        deliveryTime, rating, estimatedCost
                ));
            }
        } else {
            // Fallback: generate mock pharmacies so frontend always has data
            result.add(new PharmacyResponse(1L, "CityCare Pharmacy", "Rahul", "20 mins", 4.8, BigDecimal.valueOf(420)));
            result.add(new PharmacyResponse(2L, "MedPlus Health Hub", "Priya", "15 mins", 4.6, BigDecimal.valueOf(350)));
            result.add(new PharmacyResponse(3L, "Apollo Pharmacy Express", "Arjun", "25 mins", 4.9, BigDecimal.valueOf(510)));
        }

        System.out.println("Available pharmacies: " + result.size());
        return result;
    }

    @Override
    @Transactional
    public PharmacyOrder placeOrder(PharmacyOrderRequest request) {
        PharmacyOrder order = new PharmacyOrder();
        order.setPatientId(request.getPatientId());
        order.setPharmacistId(request.getPharmacistId());
        order.setPrescriptionId(request.getPrescriptionId());
        order.setTotalAmount(request.getTotalAmount() != null ? request.getTotalAmount() : BigDecimal.ZERO);
        order.setDeliveryEstimate(request.getDeliveryEstimate() != null ? request.getDeliveryEstimate() : "30 mins");
        order.setStatus("PENDING");

        PharmacyOrder saved = pharmacyOrderRepository.save(order);
        System.out.println("Pharmacy order placed: " + saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyOrder> getOrdersByPatientId(Long patientId) {
        List<PharmacyOrder> orders = pharmacyOrderRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        return orders != null ? orders : List.of();
    }
}
