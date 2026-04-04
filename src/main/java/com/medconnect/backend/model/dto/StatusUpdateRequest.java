package com.medconnect.backend.model.dto;

import com.medconnect.backend.model.UserStatus;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {

    @NotNull
    private UserStatus status;

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
