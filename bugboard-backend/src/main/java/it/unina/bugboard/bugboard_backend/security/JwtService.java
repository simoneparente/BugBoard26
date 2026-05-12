package it.unina.bugboard.bugboard_backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotBlank;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class JwtService {
    //TODO: Remove from code and use environment variable instead
    private static final String SECRET_KEY = "TO_BE_REMOVED_FROM_CODE_32_CHARACTERS";
    private static final int EXPIRATION_HOURS = 24;
    private static final long EXP_TIME_MILLISECONDS = TimeUnit.HOURS.toMillis(EXPIRATION_HOURS);

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(@NotBlank String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXP_TIME_MILLISECONDS))
                .signWith(getSigningKey())
                .compact();
    }
}
