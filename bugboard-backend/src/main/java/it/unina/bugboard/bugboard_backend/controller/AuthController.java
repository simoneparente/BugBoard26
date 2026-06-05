package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.AuthRequest;
import it.unina.bugboard.bugboard_backend.dto.AuthResponse;
import it.unina.bugboard.bugboard_backend.dto.AuthResult;
import it.unina.bugboard.bugboard_backend.security.SecurityConstants;
import it.unina.bugboard.bugboard_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

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
        AuthResponse responseBody = new AuthResponse(response.email());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(responseBody);
    }
}
