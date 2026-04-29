package com.medconnect.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Value("${app.name:MedConnect}")
    private String appName;

    @Value("${app.logo:/assets/logo.png}")
    private String appLogo;

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getSystemInfo() {
        return ResponseEntity.ok(Map.of(
                "appName", appName,
                "logo", appLogo,
                "version", "1.0.0",
                "environment", "production"
        ));
    }
}
