package com.medconnect.backend.controller;

import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.UserStatus;
import com.medconnect.backend.model.dto.StatusUpdateRequest;
import com.medconnect.backend.model.dto.UserResponse;
import com.medconnect.backend.repository.AppointmentRepository;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public AdminController(
            UserService userService,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalDoctors", userRepository.countByRole(Role.DOCTOR));
        stats.put("totalPatients", userRepository.countByRole(Role.PATIENT));
        stats.put("totalAppointments", appointmentRepository.count());
        stats.put("totalPharmacists", userRepository.countByRole(Role.PHARMACIST));
        stats.put("pendingApprovals", userRepository.countByStatus(UserStatus.PENDING));
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users/{filter}")
    public ResponseEntity<?> getUsersByFilter(@PathVariable String filter) {
        List<User> users;
        if (filter.equalsIgnoreCase("PENDING")) {
            System.out.println("Fetching pending users...");
            users = userRepository.findByStatus(UserStatus.PENDING);
        } else {
            try {
                Role role = Role.valueOf(filter.toUpperCase(Locale.ROOT));
                if (role == Role.DOCTOR) {
                    System.out.println("Fetching approved doctors...");
                    users = userRepository.findByRoleAndStatus(Role.DOCTOR, UserStatus.ACTIVE);
                } else {
                    users = userRepository.findByRole(role);
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid filter: " + filter);
            }
        }
        return ResponseEntity.ok(users.stream().map(UserResponse::from).toList());
    }

    @GetMapping("/users/{role}/raw")
    public List<UserResponse> getUsersByRole(@PathVariable Role role) {
        if (role == Role.DOCTOR) {
            System.out.println("Fetching approved doctors...");
            return userService.findByRoleAndStatus(Role.DOCTOR, UserStatus.ACTIVE);
        }
        return userService.findByRole(role);
    }

    @GetMapping("/approvals/pending")
    public List<UserResponse> getPendingApprovals() {
        System.out.println("Fetching pending users...");
        return userService.findByStatus(UserStatus.PENDING);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @RequestMapping(value = "/users/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request
    ) {
        String rawStatus = request.getStatus();
        if (rawStatus == null || rawStatus.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }
        UserStatus status;
        try {
            status = UserStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
        }
        return ResponseEntity.ok(userService.updateStatus(id, status));
    }
}
