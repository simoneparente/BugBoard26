package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.AuthRequest;
import it.unina.bugboard.bugboard_backend.dto.AuthResponse;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import it.unina.bugboard.bugboard_backend.security.JwtService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Dummy hash used to prevent timing attacks; generated at startup from a random value
    // so no credential-like string is committed to source.
    private static final SecureRandom RANDOM = new SecureRandom();
    private String dummyHash;

    @PostConstruct
    void initDummyHash() {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        this.dummyHash = passwordEncoder.encode(Base64.getEncoder().encodeToString(randomBytes));
    }

    public AuthResponse login(AuthRequest authRequest) {
        User user = userRepository.findByEmail(authRequest.getEmail()).orElse(null);

        String hashToCheck = (user != null) ? user.getPasswordHash() : dummyHash;
        boolean passwordMatches = passwordEncoder.matches(authRequest.getPassword(), hashToCheck);
        if (user == null || !passwordMatches) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId());
        return new AuthResponse(token, user.getEmail());
    }
}
