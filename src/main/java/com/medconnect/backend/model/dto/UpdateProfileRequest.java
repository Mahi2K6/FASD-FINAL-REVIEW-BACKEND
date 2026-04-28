package com.medconnect.backend.model.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 200)
    private String name;

    @Size(max = 32)
    private String phone;

    @Size(max = 200)
    private String specialization;

    private Integer experience;

    @Size(max = 255)
    private String emergencyContact;

    @Size(max = 500)
    private String address;

    @Size(max = 16)
    private String dob;

    @Size(max = 16)
    private String gender;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}

