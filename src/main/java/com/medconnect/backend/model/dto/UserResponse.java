package com.medconnect.backend.model.dto;

import com.medconnect.backend.model.AuthProvider;
import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.UserStatus;
import java.util.ArrayList;
import java.util.List;

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
    private String profileImageUrl;
    private String address;
    private String dob;
    private String gender;
    private List<Availability> availability = new ArrayList<>();

    public static class Availability {
        private Long id;
        private String date;
        private String time;

        public Availability() {}

        public Availability(Long id, String date, String time) {
            this.id = id;
            this.date = date;
            this.time = time;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }
    }

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
        r.setProfileImageUrl(user.getProfileImageUrl());
        r.setAddress(user.getAddress());
        r.setDob(user.getDob());
        r.setGender(user.getGender());
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    /** Alias: frontend reads avatarUrl */
    @com.fasterxml.jackson.annotation.JsonProperty("avatarUrl")
    public String getAvatarUrl() { return profileImageUrl; }

    /** Alias: frontend reads fullName */
    @com.fasterxml.jackson.annotation.JsonProperty("fullName")
    public String getFullName() { return name; }

    public List<Availability> getAvailability() {
        if (availability == null) {
            availability = new ArrayList<>();
        }
        return availability;
    }

    public void setAvailability(List<Availability> availability) {
        this.availability = availability == null ? new ArrayList<>() : availability;
    }
}
