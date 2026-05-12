package it.unina.bugboard.bugboard_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // TODO: remove from code and use environment variable instead
    private static final String SECRET_KEY = "TO_BE_REMOVED_FROM_CODE_32_CHARACTERS";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void generateToken_Success_ReturnsValidJwtString() {
        String email = "simone@test.com";

        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertFalse(token.trim().isEmpty());

        // Valid JWTs consist of three parts separated by dots
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void generateToken_Success_ContainsCorrectSubjectAndExpiration() {
        String email = "simone@test.com";
        String token = jwtService.generateToken(email);

        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));


        // Jwts.parser() throws an exception if the token is invalid or the signature doesn't match
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(email, claims.getSubject());

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        long diffInMillies = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        long diffInHours = diffInMillies / (1000 * 60 * 60);

        assertEquals(24, diffInHours);
    }
}