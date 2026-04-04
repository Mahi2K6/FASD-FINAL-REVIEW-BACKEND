package com.medconnect.backend.service.impl;

import com.medconnect.backend.model.AuthProvider;
import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.UserStatus;
import com.medconnect.backend.model.dto.AuthResponse;
import com.medconnect.backend.model.dto.LoginRequest;
import com.medconnect.backend.model.dto.RegisterRequest;
import com.medconnect.backend.model.dto.UserResponse;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.security.JwtProperties;
import com.medconnect.backend.security.JwtService;
import com.medconnect.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (req.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot self-register as ADMIN");
        }

        String email = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        if (req.getRole() == Role.DOCTOR) {
            if (req.getSpecialization() == null || req.getSpecialization().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Specialization is required for doctors");
            }
        }

        User user = new User();
        user.setName(req.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setPhone(trimToNull(req.getPhone()));
        user.setSpecialization(trimToNull(req.getSpecialization()));
        user.setExperience(req.getExperience());
        user.setEmergencyContact(trimToNull(req.getEmergencyContact()));
        user.setProvider(AuthProvider.LOCAL);

        if (req.getRole() == Role.PATIENT) {
            user.setStatus(UserStatus.ACTIVE);
        } else {
            user.setStatus(UserStatus.PENDING);
        }

        user = userRepository.save(user);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, jwtProperties.getExpirationMs(), UserResponse.from(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (user.getStatus() == UserStatus.REJECTED || user.getStatus() == UserStatus.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not permitted to sign in");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, jwtProperties.getExpirationMs(), UserResponse.from(user));
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
