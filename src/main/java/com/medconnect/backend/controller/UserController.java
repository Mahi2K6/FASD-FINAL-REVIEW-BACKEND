package com.medconnect.backend.controller;

import com.medconnect.backend.model.User;
import com.medconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // ✅ GET ALL DOCTORS (FIXED)
    @GetMapping("/doctors")
    public List<User> getDoctors() {
        return userRepository.findByRole("DOCTOR");
    }
}