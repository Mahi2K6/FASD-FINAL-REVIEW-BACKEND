package com.medconnect.backend.model.dto;

import jakarta.validation.constraints.NotBlank;

public class AgentRequest {

    @NotBlank(message = "request is required")
    private String request;

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
