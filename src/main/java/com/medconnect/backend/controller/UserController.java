package com.medconnect.backend.controller;

import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.dto.UpdateProfileRequest;
import com.medconnect.backend.model.dto.UserResponse;
import com.medconnect.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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
}
