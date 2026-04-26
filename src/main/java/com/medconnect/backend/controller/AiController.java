package com.medconnect.backend.controller;

import com.medconnect.backend.model.dto.AgentRequest;
import com.medconnect.backend.model.dto.AgentResponse;
import com.medconnect.backend.service.AiAgentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final AiAgentService aiAgentService;

    public AiController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @PostMapping("/agent-booking")
    public ResponseEntity<?> agentBooking(@Valid @RequestBody AgentRequest request, Principal principal) {
        if (request.getRequest() == null || request.getRequest().trim().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "request cannot be empty"));
        }
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        AgentResponse response = aiAgentService.processBookingRequest(request.getRequest().trim(), principal.getName());
        return ResponseEntity.ok(response);
    }
}
