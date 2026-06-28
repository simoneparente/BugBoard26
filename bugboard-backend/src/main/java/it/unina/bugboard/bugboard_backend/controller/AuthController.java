package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.AuthRequest;
import it.unina.bugboard.bugboard_backend.dto.AuthResponse;
import it.unina.bugboard.bugboard_backend.dto.AuthResult;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import it.unina.bugboard.bugboard_backend.security.SecurityConstants;
import it.unina.bugboard.bugboard_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    @GetMapping("/me")
        public ResponseEntity<AuthResponse> getCurrentUser() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            AuthResponse responseBody = new AuthResponse(
                    currentUser.getUsername(),
                    currentUser.getEmail(),
                    currentUser.getRole()
            );

            return ResponseEntity.ok(responseBody);
        }

    @SuppressWarnings("java:S2092") //TODO: remove when secure flag is true
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        long expirationTime = 24 * 60 * (long)60; // 24 hours in seconds
        AuthResult response = authService.login(request);
        ResponseCookie jwtCookie = ResponseCookie.from(SecurityConstants.JWT_COOKIE_NAME, response.token())
                .httpOnly(true) //Invisible to JS
                .secure(false) // TODO: put true in production with HTTPS
                .path("/") // Available for all endpoints
                .maxAge(expirationTime) // Expires in 24 hours (like the JWT)
                .sameSite("Strict") // Prevents CSRF attacks
                .build();
        AuthResponse responseBody = new AuthResponse(response.username(), response.email(), response.role());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(responseBody);
    }

    @SuppressWarnings("java:S2092") //TODO: remove when secure flag is true
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie jwtCookie = ResponseCookie.from(SecurityConstants.JWT_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false) // TODO: put true in production with HTTPS
                .path("/")
                .maxAge(0) // Expire the cookie immediately
                .sameSite("Strict")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .build();
    }
}
