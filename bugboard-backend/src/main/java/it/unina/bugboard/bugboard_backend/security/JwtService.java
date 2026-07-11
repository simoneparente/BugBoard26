package it.unina.bugboard.bugboard_backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class JwtService {
    private static final int EXPIRATION_HOURS = 24;
    
    private final String secretKey;
    private final Clock clock;

    public JwtService(@Value("${jwt.secret}") String secretKey, Clock clock) {
        this.secretKey = secretKey;
        this.clock = clock;
    }

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secretKey);
    }

    public String generateToken(@NotNull UUID userId) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(EXPIRATION_HOURS, ChronoUnit.HOURS);
        
        return JWT.create()
                .withSubject(userId.toString())
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(getAlgorithm());
    }

    public boolean isTokenValid(String token, UUID userId) {
        try {
            final UUID subject = extractUserId(token);
            return (subject.equals(userId)) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        JWTVerifier verifier = JWT.require(getAlgorithm()).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        return UUID.fromString(decodedJWT.getSubject());
    }

    public boolean isTokenExpired(String token) {
        JWTVerifier verifier = JWT.require(getAlgorithm()).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        
        Instant expiration = decodedJWT.getExpiresAtAsInstant(); 
        return expiration.isBefore(clock.instant());
    }
}