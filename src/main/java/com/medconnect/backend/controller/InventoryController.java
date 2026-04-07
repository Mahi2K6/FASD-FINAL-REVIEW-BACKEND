package com.medconnect.backend.controller;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.InventoryItem;
import com.medconnect.backend.repository.InventoryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    public InventoryController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping
    public List<InventoryItem> getInventory() {
        return inventoryRepository.findAll();
    }

    @PutMapping("/{id}")
    public InventoryItem updateInventory(@PathVariable Long id, @RequestBody InventoryItem request) {
        InventoryItem item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found: " + id));
        if (request.getName() != null) {
            item.setName(request.getName().trim());
        }
        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }
        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }
        return inventoryRepository.save(item);
    }
}
