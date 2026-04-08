package com.medconnect.backend.service.impl;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.InventoryItem;
import com.medconnect.backend.model.dto.InventoryCreateRequest;
import com.medconnect.backend.model.dto.InventoryUpdateRequest;
import com.medconnect.backend.repository.InventoryRepository;
import com.medconnect.backend.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItem> getAll() {
        return inventoryRepository.findAll();
    }

    @Override
    @Transactional
    public InventoryItem save(InventoryItem item) {
        validate(item);
        System.out.println("Saving medicine: " + item.getMedicineName());
        return inventoryRepository.save(item);
    }

    @Override
    @Transactional
    public InventoryItem create(InventoryCreateRequest request) {
        InventoryItem item = new InventoryItem();
        item.setMedicineName(request.getMedicineName() != null ? request.getMedicineName().trim() : null);
        item.setQuantity(request.getQuantity());
        item.setPrice(request.getPrice() != null ? BigDecimal.valueOf(request.getPrice()) : null);
        item.setUnit(request.getUnit() != null ? request.getUnit().trim() : null);
        item.setCategory(request.getCategory() != null ? request.getCategory().trim() : null);
        item.setMinThreshold(request.getMinThreshold() != null ? request.getMinThreshold() : 10);
        return save(item);
    }

    @Override
    @Transactional
    public InventoryItem update(Long id, InventoryUpdateRequest request) {
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }
        if (request.getPrice() != null) {
            item.setPrice(BigDecimal.valueOf(request.getPrice()));
        }
        if (request.getMinThreshold() != null) {
            item.setMinThreshold(request.getMinThreshold());
        }
        return save(item);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        inventoryRepository.delete(item);
    }

    private void validate(InventoryItem item) {
        if (item.getMedicineName() == null || item.getMedicineName().trim().isEmpty()) {
            throw new RuntimeException("Medicine name is required");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be at least 1");
        }
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Price must be greater than 0");
        }
    }
}
