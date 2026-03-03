package com.medconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medconnect.backend.model.User;
import com.medconnect.backend.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Fetch User Details
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    // Update Profile & SOS Contact
    @PutMapping("/{id}/profile")
    public User updateProfile(@PathVariable Long id, @RequestBody User updatedData) {
        User user = userRepository.findById(id).orElseThrow();
        
        if (updatedData.getFullName() != null) user.setFullName(updatedData.getFullName());
        if (updatedData.getPhoneNumber() != null) user.setPhoneNumber(updatedData.getPhoneNumber());
        if (updatedData.getEmergencyContact() != null) user.setEmergencyContact(updatedData.getEmergencyContact());
        if (updatedData.getAddress() != null) user.setAddress(updatedData.getAddress());
        
        return userRepository.save(user);
    }

    // NEW: Change Password (For Profile Dropdown)
    @PutMapping("/{id}/change-password")
    public User changePassword(@PathVariable Long id, @RequestBody String newPassword) {
        User user = userRepository.findById(id).orElseThrow();
        user.setPassword(newPassword); // In a production app, hash this with BCrypt!
        return userRepository.save(user);
    }

    // Upgrade Subscription Plan
    @PutMapping("/{id}/subscription")
    public User upgradeSubscription(@PathVariable Long id, @RequestBody String plan) {
        User user = userRepository.findById(id).orElseThrow();
        user.setSubscriptionPlan(plan);
        return userRepository.save(user);
    }

    // NEW: Global Search (For the Samsung-style Bottom Bar)
    @GetMapping("/search")
    public List<User> searchDoctors(@RequestParam String query) {
        // Simple search for doctors by name or specialization
        return userRepository.findByRole("DOCTOR").stream()
                .filter(doc -> doc.getFullName().toLowerCase().contains(query.toLowerCase()) || 
                              (doc.getSpecialization() != null && doc.getSpecialization().toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
    }
}