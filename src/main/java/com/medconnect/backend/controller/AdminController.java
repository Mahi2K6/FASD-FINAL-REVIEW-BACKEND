package com.medconnect.backend.controller;

import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.UserStatus;
import com.medconnect.backend.model.dto.StatusUpdateRequest;
import com.medconnect.backend.model.dto.UserResponse;
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

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("DOCTOR", userService.countByRole(Role.DOCTOR));
        stats.put("PATIENT", userService.countByRole(Role.PATIENT));
        stats.put("PHARMACIST", userService.countByRole(Role.PHARMACIST));
        return stats;
    }

    @GetMapping("/users/{role}")
    public List<UserResponse> getUsersByRole(@PathVariable Role role) {
        return userService.findByRole(role);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PutMapping("/users/{id}/status")
    @PatchMapping("/users/{id}/status")
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
