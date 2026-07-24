package it.unina.bugboard.bugboard_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SEED_USERNAME:bugboard}")
    private String seedUsername;

    @Value("${SEED_EMAIL:bugboard@bugboard.it}")
    private String seedEmail;

    @Value("${SEED_PASSWORD:bugboard}")
    private String seedPassword;

    @Override
    @Transactional
    public void run(String... args) {
        // Populate only if the database is empty
        if (userRepository.count() > 0) {
            return;
        }

        // Create admin user
        userRepository.save(User.builder()
                .username(seedUsername)
                .email(seedEmail)
                .passwordHash(passwordEncoder.encode(seedPassword))
                .role(Role.ADMIN)
                .build());
    }
}
