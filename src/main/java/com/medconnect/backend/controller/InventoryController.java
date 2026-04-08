package com.medconnect.backend.controller;

import com.medconnect.backend.model.InventoryItem;
import com.medconnect.backend.model.dto.InventoryUpdateRequest;
import com.medconnect.backend.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public List<InventoryItem> getInventory() {
        return inventoryService.getAll();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public InventoryItem createInventory(@Valid @RequestBody InventoryItem item) {
        System.out.println("Inventory item received: " + item.getMedicineName());
        return inventoryService.save(item);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public InventoryItem updateInventory(@PathVariable Long id, @RequestBody InventoryUpdateRequest request) {
        return inventoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PHARMACIST','ADMIN')")
    public ResponseEntity<?> deleteInventory(@PathVariable Long id) {
        inventoryService.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }
}
