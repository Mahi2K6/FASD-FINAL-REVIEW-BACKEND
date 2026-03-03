package com.medconnect.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userid;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // "PATIENT", "DOCTOR", "PHARMACIST", "ADMIN"

    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;
    private String pincode;
    private String specialization; 

    // --- SAAS & SECURITY FEATURES ---
    private String approvalStatus = "APPROVED"; 
    private String subscriptionPlan = "FREE"; 
    private String emergencyContact; 

    // NEW: License Verification for Professionals
    private String licenseId;

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }

    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getLicenseId() { return licenseId; }
    public void setLicenseId(String licenseId) { this.licenseId = licenseId; }
}