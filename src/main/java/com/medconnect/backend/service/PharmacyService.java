package com.medconnect.backend.service;

import com.medconnect.backend.model.PharmacyOrder;
import com.medconnect.backend.model.dto.PharmacyOrderRequest;
import com.medconnect.backend.model.dto.PharmacyResponse;

import java.util.List;

public interface PharmacyService {

    List<PharmacyResponse> getAvailablePharmacies();

    PharmacyOrder placeOrder(PharmacyOrderRequest request);

    List<PharmacyOrder> getOrdersByPatientId(Long patientId);
}
