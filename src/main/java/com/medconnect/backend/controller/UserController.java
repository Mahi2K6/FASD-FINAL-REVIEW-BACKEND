package com.medconnect.backend.controller;

import org.springframework.http.ResponseEntity;

import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.dto.ChangePasswordRequest;
import com.medconnect.backend.model.dto.UpdateProfileRequest;
import com.medconnect.backend.model.dto.UserResponse;
import com.medconnect.backend.service.UserService;
import com.medconnect.backend.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final UserServiceImpl userServiceImpl;

    public UserController(UserService userService, UserServiceImpl userServiceImpl) {
        this.userService = userService;
        this.userServiceImpl = userServiceImpl;
    }

    // ── List all users (admin context needs this) ──

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/doctors")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getDoctors() {
        return ResponseEntity.ok(userService.findByRole(Role.DOCTOR));
    }

    @GetMapping("/patients")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getPatients() {
        return ResponseEntity.ok(userService.findByRole(Role.PATIENT));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // ── GET own profile (/profile or /me) ──

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getProfile(Principal principal) {
        return ResponseEntity.ok(userService.getByEmail(principal.getName().trim().toLowerCase()));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getMe(Principal principal) {
        return ResponseEntity.ok(userService.getByEmail(principal.getName().trim().toLowerCase()));
    }

    // ── UPDATE profile (/profile or /update-profile) ──

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfile(
            Principal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName().trim().toLowerCase(), request));
    }

    @PutMapping("/update-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfileAlias(
            Principal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName().trim().toLowerCase(), request));
    }

    // ── Change password ──

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        try {
            userServiceImpl.changePassword(
                    principal.getName().trim().toLowerCase(),
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    // ── Profile image upload ──

    @PutMapping("/profile/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfileImage(
            Principal principal,
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Path dir = Paths.get("uploads");
            Files.createDirectories(dir);
            String original = file.getOriginalFilename();
            String extension = "";
            if (original != null) {
                int idx = original.lastIndexOf('.');
                if (idx >= 0) {
                    extension = original.substring(idx);
                }
            }
            String saved = UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), dir.resolve(saved), StandardCopyOption.REPLACE_EXISTING);
            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/")
                    .path(saved)
                    .toUriString();
            return ResponseEntity.ok(userService.updateProfileImage(principal.getName().trim().toLowerCase(), url));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}