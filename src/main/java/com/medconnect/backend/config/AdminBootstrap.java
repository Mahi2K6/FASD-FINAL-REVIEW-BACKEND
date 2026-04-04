package com.medconnect.backend.config;

import com.medconnect.backend.model.AuthProvider;
import com.medconnect.backend.model.Role;
import com.medconnect.backend.model.User;
import com.medconnect.backend.model.UserStatus;
import com.medconnect.backend.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Optional one-time style bootstrap for an ADMIN user (dev/demo only).
 * Enable with: app.bootstrap-admin=true (and set app.bootstrap-admin-email / password).
 */
@Component
@Order(100)
@ConditionalOnProperty(name = "app.bootstrap-admin", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = System.getenv().getOrDefault("ADMIN_EMAIL", "admin@medconnect.local");
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User admin = new User();
        admin.setName("Administrator");
        admin.setEmail(email);
        String raw = System.getenv().getOrDefault("ADMIN_PASSWORD", "ChangeMeAdmin123!");
        admin.setPassword(passwordEncoder.encode(raw));
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setProvider(AuthProvider.LOCAL);
        userRepository.save(admin);
    }
}
