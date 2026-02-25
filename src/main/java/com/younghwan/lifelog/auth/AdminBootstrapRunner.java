package com.younghwan.lifelog.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_USERNAME:}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminUsername == null || adminUsername.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.info("ADMIN_USERNAME / ADMIN_PASSWORD not set. Skipping admin bootstrap.");
            return;
        }

        userAccountRepository.findByUsername(adminUsername).ifPresentOrElse(
                user -> log.info("Admin user '{}' already exists.", adminUsername),
                () -> {
                    UserAccount admin = UserAccount.builder()
                            .username(adminUsername)
                            .passwordHash(passwordEncoder.encode(adminPassword))
                            .role(UserRole.ADMIN)
                            .status(UserStatus.APPROVED)
                            .build();
                    userAccountRepository.save(admin);
                    log.info("Bootstrapped admin user '{}'.", adminUsername);
                }
        );
    }
}
