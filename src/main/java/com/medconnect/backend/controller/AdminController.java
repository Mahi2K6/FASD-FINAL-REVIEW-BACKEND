package com.medconnect.backend.controller;

import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.UserStatus;
import com.medconnect.backend.model.dto.StatusUpdateRequest;
import com.medconnect.backend.model.dto.UserResponse;
import com.medconnect.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    @PutMapping("/users/{userId}/status")
    public UserResponse updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody StatusUpdateRequest body
    ) {
        return userService.updateStatus(userId, body.getStatus());
    }

    @PatchMapping("/users/{id}/status")
    public UserResponse patchUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest body
    ) {
        return userService.updateStatus(id, body.getStatus());
    }
}
