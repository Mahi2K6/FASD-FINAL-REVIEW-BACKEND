package com.medconnect.backend.controller;

import com.medconnect.backend.model.PharmacyOrder;
import com.medconnect.backend.model.PharmacyOrderItem;
import com.medconnect.backend.model.Prescription;
import com.medconnect.backend.model.PrescriptionMedicine;
import com.medconnect.backend.model.User;
import com.medconnect.backend.repository.PharmacyOrderRepository;
import com.medconnect.backend.repository.PrescriptionRepository;
import com.medconnect.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PharmacyOrderRepository pharmacyOrderRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;

    public OrderController(PharmacyOrderRepository pharmacyOrderRepository, PrescriptionRepository prescriptionRepository, UserRepository userRepository) {
        this.pharmacyOrderRepository = pharmacyOrderRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getOrders() {
        return List.of();
    }

    @PostMapping("/create-from-prescription")
    public ResponseEntity<?> createFromPrescription(@RequestBody Map<String, Long> request, java.security.Principal principal) {
        Long patientId = request.get("patientId");
        Long prescriptionId = request.get("prescriptionId");

        if (patientId == null || prescriptionId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "patientId and prescriptionId are required"));
        }

        User user = userRepository.findByEmail(principal.getName().trim().toLowerCase())
                .orElse(null);
        if (user == null || (!user.getId().equals(patientId) && !"DOCTOR".equals(user.getRole()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized access to order creation"));
        }

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElse(null);

        if (prescription == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Prescription not found"));
        }

        if (!prescription.getPatientId().equals(patientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Prescription does not belong to this patient"));
        }

        PharmacyOrder order = new PharmacyOrder();
        order.setPatientId(patientId);
        order.setPrescriptionId(prescriptionId);
        order.setStatus("PENDING");
        order.setDeliveryEstimate("2-3 Business Days");

        BigDecimal total = BigDecimal.ZERO;

        if (prescription.getMedicines() != null) {
            for (PrescriptionMedicine pMed : prescription.getMedicines()) {
                PharmacyOrderItem item = new PharmacyOrderItem();
                item.setOrder(order);
                item.setMedicineName(pMed.getMedicineName());
                item.setDosage(pMed.getDosage());
                item.setFrequency(pMed.getFrequency());
                item.setDuration(pMed.getDuration());
                item.setInstructions(pMed.getInstructions());
                // Dummy price estimation
                BigDecimal dummyPrice = new BigDecimal("15.50");
                item.setPrice(dummyPrice);
                order.getItems().add(item);
                total = total.add(dummyPrice);
            }
        }
        order.setTotalAmount(total);

        PharmacyOrder saved = pharmacyOrderRepository.save(order);
        System.out.println("Order created from prescription: " + prescriptionId);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
