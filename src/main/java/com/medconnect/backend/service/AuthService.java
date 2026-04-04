package com.medconnect.backend.service;

import com.medconnect.backend.model.dto.AuthResponse;
import com.medconnect.backend.model.dto.LoginRequest;
import com.medconnect.backend.model.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
