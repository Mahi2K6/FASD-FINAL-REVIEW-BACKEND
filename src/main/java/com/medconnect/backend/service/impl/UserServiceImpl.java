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
import com.medconnect.backend.repository.DoctorAvailabilityRepository;
import com.medconnect.backend.repository.UserRepository;
import com.medconnect.backend.service.NotificationService;
import com.medconnect.backend.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            NotificationService notificationService,
            AppointmentRepository appointmentRepository,
            PrescriptionRepository prescriptionRepository,
            NotificationRepository notificationRepository,
            HealthMetricRepository healthMetricRepository,
            DoctorAvailabilityRepository doctorAvailabilityRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.notificationRepository = notificationRepository;
        this.healthMetricRepository = healthMetricRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByRole(Role role) {
        if (role == Role.DOCTOR) {
            return userRepository.findByRoleAndStatus(Role.DOCTOR, UserStatus.ACTIVE).stream()
                    .map(this::mapUserToResponseWithAvailability)
                    .toList();
        }
        return userRepository.findByRole(role).stream().map(this::mapUserToResponseWithAvailability).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByRoleAndStatus(Role role, UserStatus status) {
        return userRepository.findByRoleAndStatus(role, status).stream().map(this::mapUserToResponseWithAvailability).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findByStatus(UserStatus status) {
        return userRepository.findByStatus(status).stream().map(this::mapUserToResponseWithAvailability).toList();
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
        return mapUserToResponseWithAvailability(user);
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
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress().trim());
        }
        if (request.getDob() != null) {
            user.setDob(request.getDob().trim());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender().trim());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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
        return userRepository.findAll().stream().map(this::mapUserToResponseWithAvailability).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(this::mapUserToResponseWithAvailability)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponse mapUserToResponseWithAvailability(User user) {
        UserResponse response = UserResponse.from(user);
        if (user.getRole() == Role.DOCTOR) {
            List<com.medconnect.backend.model.DoctorAvailability> dbSlots = 
                doctorAvailabilityRepository.findUpcomingAvailableSlots(user.getId(), java.time.LocalDate.now(), java.time.LocalTime.now());
            
            List<UserResponse.Availability> availabilities = new java.util.ArrayList<>();
            if (dbSlots != null && !dbSlots.isEmpty()) {
                for (com.medconnect.backend.model.DoctorAvailability slot : dbSlots) {
                    availabilities.add(new UserResponse.Availability(
                        slot.getId(),
                        slot.getSlotDate().toString(),
                        String.format("%02d:%02d", slot.getStartTime().getHour(), slot.getStartTime().getMinute())
                    ));
                }
            } else {
                java.time.LocalDate today = java.time.LocalDate.now();
                availabilities.add(new UserResponse.Availability(1L, today.plusDays(1).toString(), "10:00"));
                availabilities.add(new UserResponse.Availability(2L, today.plusDays(1).toString(), "14:00"));
                availabilities.add(new UserResponse.Availability(3L, today.plusDays(2).toString(), "11:30"));
            }
            
            System.out.println("Doctor: " + response.getName());
            System.out.println("Availability: " + availabilities);
            
            response.setAvailability(availabilities);
        }
        return response;
    }
}
