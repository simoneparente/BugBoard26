package it.unina.bugboard.bugboard_backend.config;

import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
    public void run(String... args) {
        if (userRepository.findByEmail(seedEmail).isEmpty()) {
            User admin = User.builder()
                    .username(seedUsername)
                    .email(seedEmail)
                    .passwordHash(passwordEncoder.encode(seedPassword))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }
}
