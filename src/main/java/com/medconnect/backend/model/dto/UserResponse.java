package com.medconnect.backend.model.dto;

import com.medconnect.backend.model.AuthProvider;
import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.UserStatus;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private UserStatus status;
    private String specialization;
    private Integer experience;
    private AuthProvider provider;
    private String emergencyContact;

    public static UserResponse from(com.medconnect.backend.model.User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setName(user.getName());
        r.setEmail(user.getEmail());
        r.setRole(user.getRole());
        r.setPhone(user.getPhone());
        r.setStatus(user.getStatus());
        r.setSpecialization(user.getSpecialization());
        r.setExperience(user.getExperience());
        r.setProvider(user.getProvider());
        r.setEmergencyContact(user.getEmergencyContact());
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}
