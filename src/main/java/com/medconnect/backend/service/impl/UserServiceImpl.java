package com.medconnect.backend.service.impl;

import com.medconnect.backend.exception.ResourceNotFoundException;
import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.UserStatus;
import com.medconnect.backend.model.dto.UpdateProfileRequest;
import com.medconnect.backend.model.dto.UserResponse;
import com.medconnect.backend.repository.AppointmentRepository;
import com.medconnect.backend.repository.HealthMetricRepository;
import com.medconnect.backend.repository.NotificationRepository;
import com.medconnect.backend.repository.PrescriptionRepository;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.NotificationService;
import com.medconnect.backend.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final NotificationRepository notificationRepository;
    private final HealthMetricRepository healthMetricRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            NotificationService notificationService,
            AppointmentRepository appointmentRepository,
            PrescriptionRepository prescriptionRepository,
            NotificationRepository notificationRepository,
            HealthMetricRepository healthMetricRepository
    ) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.notificationRepository = notificationRepository;
        this.healthMetricRepository = healthMetricRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByRole(Role role) {
        if (role == Role.DOCTOR) {
            return userRepository.findByRoleAndStatus(Role.DOCTOR, UserStatus.ACTIVE).stream()
                    .map(UserResponse::from)
                    .toList();
        }
        return userRepository.findByRole(role).stream().map(UserResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByRoleAndStatus(Role role, UserStatus status) {
        return userRepository.findByRoleAndStatus(role, status).stream().map(UserResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByStatus(UserStatus status) {
        return userRepository.findByStatus(status).stream().map(UserResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setStatus(status);
        User saved = userRepository.save(user);
        if (status == UserStatus.ACTIVE) {
            notificationService.createNotification(
                    saved.getId(),
                    "Account Approved",
                    "Your account has been approved. You can now login.",
                    "APPROVAL"
            );
        }
        return UserResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User deleteUserByAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("Cannot delete admin");
        }
        appointmentRepository.deleteByPatientId(user.getId());
        appointmentRepository.deleteByDoctorId(user.getId());
        prescriptionRepository.deleteByPatientId(user.getId());
        prescriptionRepository.deleteByDoctorId(user.getId());
        healthMetricRepository.deleteByPatientId(user.getId());
        notificationRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (request.getName() != null) {
            user.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getSpecialization() != null) {
            user.setSpecialization(request.getSpecialization().trim());
        }
        if (request.getExperience() != null) {
            user.setExperience(request.getExperience());
        }
        if (request.getEmergencyContact() != null) {
            user.setEmergencyContact(request.getEmergencyContact().trim());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateProfileImage(String email, String profileImageUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        user.setProfileImageUrl(profileImageUrl);
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
