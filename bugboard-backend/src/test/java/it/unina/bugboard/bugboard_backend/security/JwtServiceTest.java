package it.unina.bugboard.bugboard_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class JwtServiceTest {

    private JwtService jwtService;

    // Test-only secret; not used in production. Must be at least 32 bytes for HS256.
    private static final String SECRET_KEY = "test-secret-key-for-unit-tests-only-32b";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET_KEY);
    }

    @Test
    void generateToken_Success_ReturnsValidJwtString() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateToken(userId);

        assertNotNull(token);
        assertFalse(token.trim().isEmpty());

        // Valid JWTs consist of three parts separated by dots
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void generateToken_Success_ContainsCorrectSubjectAndExpiration() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId);

        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));


        // Jwts.parser() throws an exception if the token is invalid or the signature doesn't match
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(userId.toString(), claims.getSubject());

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        long diffInMillies = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        long diffInHours = diffInMillies / (1000 * 60 * 60);

        assertEquals(24, diffInHours);
    }

    @Test
    void isTokenValid_Success_ReturnsTrue() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId);
        boolean tokenValid = jwtService.isTokenValid(token, userId);

        assertTrue(tokenValid);
    }

    @Test
    void isTokenValid_Success_ReturnsFalse() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId);
        boolean tokenValid = jwtService.isTokenValid(token, UUID.randomUUID());

        assertFalse(tokenValid);
    }

    @Test
    void isTokenValid_Success_ReturnsFalse_WhenTokenIsExpired() {
        JwtService spyJwtService = spy(jwtService);
        UUID userId = UUID.randomUUID();

        String token = spyJwtService.generateToken(userId);
        doReturn(true).when(spyJwtService).isTokenExpired(token);

        boolean tokenValid = spyJwtService.isTokenValid(token, userId);
        assertFalse(tokenValid);
    }
}