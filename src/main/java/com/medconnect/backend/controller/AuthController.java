package com.medconnect.backend.controller;

import com.medconnect.backend.model.dto.AuthResponse;
import com.medconnect.backend.model.dto.LoginRequest;
import com.medconnect.backend.model.dto.RegisterRequest;
import com.medconnect.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final String HEALTH_MESSAGE = "Auth API is working";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Public health check for verifying the backend is reachable (e.g. from a browser).
     */
    @GetMapping("/test")
    public ResponseEntity<String> authHealth() {
        return ResponseEntity.ok(HEALTH_MESSAGE);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse body = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
