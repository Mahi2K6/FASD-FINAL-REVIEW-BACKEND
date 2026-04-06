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
        
        // Delete and recreate admin to ensure password is correctly BCrypt encoded
        userRepository.findByEmail("admin@medconnect.com")
            .ifPresent(userRepository::delete);

        User admin = new User();
        admin.setName("Super Admin");
        admin.setEmail("admin@medconnect.com");
        admin.setPassword(passwordEncoder.encode("Admin@1234"));
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setProvider(AuthProvider.LOCAL);
        admin.setPhone("0000000000");
        userRepository.save(admin);

        System.out.println("✅ Admin created: admin@medconnect.com / Admin@1234");
    }
}
