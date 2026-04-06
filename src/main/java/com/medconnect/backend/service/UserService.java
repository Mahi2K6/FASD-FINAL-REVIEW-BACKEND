package com.medconnect.backend.service;

import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.UserStatus;
import com.medconnect.backend.model.dto.UpdateProfileRequest;
import com.medconnect.backend.model.dto.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> findByRole(Role role);

    List<UserResponse> findByRoleAndStatus(Role role, UserStatus status);

    List<UserResponse> findByStatus(UserStatus status);

    long countByRole(Role role);

    UserResponse updateStatus(Long userId, UserStatus status);

    void deleteUser(Long id);

    UserResponse getByEmail(String email);

    UserResponse updateProfile(String email, UpdateProfileRequest request);
}
