package com.medconnect.backend.service;

import com.medconnect.backend.model.InventoryItem;
import com.medconnect.backend.model.dto.InventoryCreateRequest;
import com.medconnect.backend.model.dto.InventoryUpdateRequest;

import java.util.List;

public interface InventoryService {

    List<InventoryItem> getAll();

    InventoryItem save(InventoryItem item);

    InventoryItem create(InventoryCreateRequest request);

    InventoryItem update(Long id, InventoryUpdateRequest request);

    void delete(Long id);
}
