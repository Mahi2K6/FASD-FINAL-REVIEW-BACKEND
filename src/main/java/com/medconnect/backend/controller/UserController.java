package com.medconnect.backend.controller;

import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.dto.UpdateProfileRequest;
import com.medconnect.backend.model.dto.UserResponse;
import com.medconnect.backend.service.UserService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/doctors")
    @PreAuthorize("isAuthenticated()")
    public List<UserResponse> getDoctors() {
        return userService.findByRole(Role.DOCTOR);
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public UserResponse getProfile(Principal principal) {
        return userService.getByEmail(principal.getName().trim().toLowerCase());
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateProfile(
            Principal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateProfile(principal.getName().trim().toLowerCase(), request);
    }

    @PutMapping("/profile/image")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateProfileImage(
            Principal principal,
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
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
            return userService.updateProfileImage(principal.getName().trim().toLowerCase(), url);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store profile image", ex);
        }
    }
}
