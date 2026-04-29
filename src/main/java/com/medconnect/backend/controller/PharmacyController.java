package com.medconnect.backend.controller;

import com.medconnect.backend.model.PharmacyOrder;
import com.medconnect.backend.model.dto.PharmacyOrderRequest;
import com.medconnect.backend.model.dto.PharmacyResponse;
import com.medconnect.backend.service.PharmacyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PharmacyController {

    private final PharmacyService pharmacyService;

    public PharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @GetMapping("/api/pharmacies/available")
    public ResponseEntity<List<PharmacyResponse>> getAvailablePharmacies() {
        List<PharmacyResponse> pharmacies = pharmacyService.getAvailablePharmacies();
        return ResponseEntity.ok(pharmacies);
    }

    @PostMapping("/api/pharmacy/order")
    public ResponseEntity<?> placeOrder(@RequestBody PharmacyOrderRequest request) {
        try {
            if (request.getPatientId() == null || request.getPrescriptionId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "patientId and prescriptionId are required"));
            }
            PharmacyOrder order = pharmacyService.placeOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/api/pharmacy/orders/{patientId}")
    public ResponseEntity<List<PharmacyOrder>> getOrdersByPatientId(@PathVariable Long patientId) {
        List<PharmacyOrder> orders = pharmacyService.getOrdersByPatientId(patientId);
        return ResponseEntity.ok(orders != null ? orders : List.of());
    }
}
