package it.unina.bugboard.bugboard_backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private Clock clock;

    // Test-only secret; not used in production. Must be at least 32 bytes for HMAC256.
    private static final String SECRET_KEY = "test-secret-key-for-unit-tests-only-32b";

    @SuppressWarnings("java:S8692")
    @BeforeEach
    void setUp() {
        // The system clock is used here because the JWT library's verification process
        // strictly checks the expiration claim against the real system time. 
        // Using a fixed past clock breaks the verification.
        clock = Clock.systemUTC();
        jwtService = new JwtService(SECRET_KEY, clock);
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

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        JWTVerifier verifier = JWT.require(algorithm).build();

        // verifier.verify() throws an exception if the token is invalid or signature doesn't match
        DecodedJWT decodedJWT = verifier.verify(token);

        assertEquals(userId.toString(), decodedJWT.getSubject());
        assertNotNull(decodedJWT.getIssuedAtAsInstant());
        assertNotNull(decodedJWT.getExpiresAtAsInstant());

        // Calculate the difference between issue time and expiration time
        long diffInHours = ChronoUnit.HOURS.between(
                decodedJWT.getIssuedAtAsInstant(),
                decodedJWT.getExpiresAtAsInstant()
        );

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
    void isTokenValid_Failure_ReturnsFalse_WhenUserIdDoesNotMatch() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId);
        
        // Attempting to validate the token against a different, random UUID
        boolean tokenValid = jwtService.isTokenValid(token, UUID.randomUUID());

        assertFalse(tokenValid);
    }

    @Test
    void isTokenValid_Failure_ReturnsFalse_WhenTokenIsExpired() {
        // 1. Setup a fixed clock at a specific moment in time
        Instant fixedInstant = Instant.parse("2026-06-23T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));
        
        // 2. Create the service with the fixed clock and generate the token
        JwtService fixedTimeJwtService = new JwtService(SECRET_KEY, fixedClock);
        UUID userId = UUID.randomUUID();
        String token = fixedTimeJwtService.generateToken(userId);

        // 3. Fast-forward time by 25 hours (past the 24-hour token expiration limit)
        Instant futureInstant = fixedInstant.plus(25, ChronoUnit.HOURS);
        Clock futureClock = Clock.fixed(futureInstant, ZoneId.of("UTC"));
        
        // 4. Create a new service instance operating in the "future" to validate the old token
        JwtService futureTimeJwtService = new JwtService(SECRET_KEY, futureClock);
        
        boolean tokenValid = futureTimeJwtService.isTokenValid(token, userId);

        assertFalse(tokenValid); // Should be false because the token has expired
    }
}