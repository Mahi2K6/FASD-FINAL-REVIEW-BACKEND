package com.medconnect.backend.config;

import com.medconnect.backend.model.InventoryItem;
import com.medconnect.backend.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(2)
public class InventoryDataSeeder implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;

    public InventoryDataSeeder(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) {
        if (inventoryRepository.count() > 0) {
            return;
        }
        List<InventoryItem> seed = List.of(
                item("Amoxicillin 500mg", 150, "12.50", "tablets", "Antibiotic", 20),
                item("Paracetamol 500mg", 500, "2.00", "tablets", "Painkiller", 50),
                item("Metformin 500mg", 200, "8.00", "tablets", "Diabetes", 30),
                item("Atorvastatin 10mg", 80, "25.00", "tablets", "Cardiac", 15),
                item("Omeprazole 20mg", 120, "15.00", "tablets", "Gastric", 20),
                item("Cetirizine 10mg", 45, "5.00", "tablets", "Antihistamine", 20),
                item("Azithromycin 250mg", 60, "35.00", "tablets", "Antibiotic", 10),
                item("Ibuprofen 400mg", 8, "6.00", "tablets", "Painkiller", 20)
        );
        inventoryRepository.saveAll(seed);
    }

    private static InventoryItem item(
            String medicineName,
            int quantity,
            String price,
            String unit,
            String category,
            int minThreshold
    ) {
        InventoryItem i = new InventoryItem();
        i.setMedicineName(medicineName);
        i.setQuantity(quantity);
        i.setPrice(new BigDecimal(price));
        i.setUnit(unit);
        i.setCategory(category);
        i.setMinThreshold(minThreshold);
        return i;
    }
}
