package com.medconnect.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.medconnect.backend.model.User;
import com.medconnect.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") 
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        
        // 1. SECURITY BLOCK: No Admin registration
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Security Alert: Admin registration is blocked.");
        }

        // 2. Prevent Duplicate UserIDs
        if (userRepository.findByUserid(user.getUserid()) != null) {
            throw new RuntimeException("UserID already exists!");
        }

        // 3. NEW: Require License ID for Professionals
        if ("DOCTOR".equalsIgnoreCase(user.getRole()) || "PHARMACIST".equalsIgnoreCase(user.getRole())) {
            if (user.getLicenseId() == null || user.getLicenseId().trim().isEmpty()) {
                throw new RuntimeException("A valid Medical/Pharmacy License ID is required for registration.");
            }
            user.setApprovalStatus("PENDING"); 
        } else {
            // Patients
            user.setApprovalStatus("APPROVED"); 
            user.setSubscriptionPlan("FREE");   
        }

        return userRepository.save(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody User loginUser) {
        User user = userRepository.findByUserid(loginUser.getUserid());
        
        if (user != null && user.getPassword().equals(loginUser.getPassword())) {
            return user;
        }
        return null;
    }
}