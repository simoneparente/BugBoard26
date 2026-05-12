package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.AuthRequest;
import it.unina.bugboard.bugboard_backend.dto.AuthResponse;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import it.unina.bugboard.bugboard_backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_Success_ReturnsAuthResponseWithToken() {
        AuthRequest request = new AuthRequest("mario@example.com", "SafePassword123#!");

        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .email("mario@example.com")
                .passwordHash("hash_in_db")
                .role(Role.TECHNICAL)
                .build();
        
        when(userRepository.findByEmail("mario@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("SafePassword123#!", "hash_in_db")).thenReturn(true);
        when(jwtService.generateToken("mario@example.com")).thenReturn("mocked.jwt.token");
        
        AuthResponse response = authService.login(request);
        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("mario@example.com", response.getEmail());
    }

    @Test
    void login_ThrowsBadCredentials_WhenEmailNotFound() {
        AuthRequest request = new AuthRequest("not_found@example.com", "Password123!");

        when(userRepository.findByEmail("not_found@example.com")).thenReturn(Optional.empty());
        
        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> {
            authService.login(request);
        });

        assertEquals("Invalid email or password", ex.getMessage());

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void login_ThrowsBadCredentials_WhenPasswordIsWrong() {
        AuthRequest request = new AuthRequest("mario@example.com", "WrongPassword!");

        User mockUser = User.builder()
                .email("mario@example.com")
                .passwordHash("hash_in_db")
                .build();
        when(userRepository.findByEmail("mario@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("WrongPassword!", "hash_in_db")).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> {
            authService.login(request);
        });

        assertEquals("Invalid email or password", ex.getMessage());
        verify(jwtService, never()).generateToken(anyString());
    }
}