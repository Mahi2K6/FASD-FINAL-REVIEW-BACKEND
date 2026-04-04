package com.medconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medconnect.backend.model.User;
import com.medconnect.backend.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("DOCTOR", userRepository.countByRole("DOCTOR"));
        stats.put("PATIENT", userRepository.countByRole("PATIENT"));
        stats.put("PHARMACIST", userRepository.countByRole("PHARMACIST"));
        return stats;
    }

    @GetMapping("/users/{role}")
    public List<User> getUsersByRole(@PathVariable String role) {
        return userRepository.findByRole(role);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    // --- NEW SAAS FEATURE: Approve/Reject Users ---
    @PutMapping("/users/{userId}/approval")
    public User updateApprovalStatus(@PathVariable Long userId, @RequestBody String status) {
        // Status should be "APPROVED" or "REJECTED"
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setApprovalStatus(status);
        return userRepository.save(user);
    }
}