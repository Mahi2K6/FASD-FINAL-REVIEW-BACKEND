package com.medconnect.backend.controller;

import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.UserStatus;
import com.medconnect.backend.model.dto.UserResponse;
import com.medconnect.backend.repository.AppointmentRepository;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
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
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        System.out.println("Admin action: delete user " + id);
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body("User not found");
        }
        User user = userRepository.findById(id).orElseThrow();
        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.status(403).body("Cannot delete admin");
        }
        userService.deleteUserByAdmin(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @RequestMapping(value = "/users/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String statusStr = body.get("status");
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Status is required");
        }
        UserStatus status;
        try {
            status = UserStatus.valueOf(statusStr.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid status");
        }
        System.out.println("Admin action: update status " + id + " -> " + status);
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body("User not found");
        }
        UserResponse updated = userService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}
