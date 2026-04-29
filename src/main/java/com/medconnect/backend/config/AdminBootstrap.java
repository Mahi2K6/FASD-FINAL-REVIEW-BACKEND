package com.medconnect.backend.config;

import com.medconnect.backend.model.AuthProvider;
import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.UserStatus;
import com.medconnect.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap {

    @Value("${app.bootstrap-admin:true}")
    private boolean bootstrapAdmin;

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (!bootstrapAdmin) return;
        
        userRepository.findByEmail("admin123@medconnect.com").ifPresentOrElse(admin -> {
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            userRepository.save(admin);
            System.out.println("✅ Admin updated: admin123@medconnect.com / admin123");
        }, () -> {
            User admin = new User();
            admin.setName("Super Admin");
            admin.setEmail("admin123@medconnect.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setProvider(AuthProvider.LOCAL);
            admin.setPhone("0000000000");
            userRepository.save(admin);
            System.out.println("✅ Admin created: admin123@medconnect.com / admin123");
        });
    }
}
