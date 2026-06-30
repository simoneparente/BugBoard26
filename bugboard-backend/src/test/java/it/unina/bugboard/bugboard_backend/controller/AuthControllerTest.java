package it.unina.bugboard.bugboard_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.unina.bugboard.bugboard_backend.config.SecurityConfig;
import it.unina.bugboard.bugboard_backend.dto.AuthRequest;
import it.unina.bugboard.bugboard_backend.dto.AuthResult;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import it.unina.bugboard.bugboard_backend.security.CustomAccessDeniedHandler;
import it.unina.bugboard.bugboard_backend.security.JwtAuthenticationEntryPoint;
import it.unina.bugboard.bugboard_backend.security.JwtAuthenticationFilter;
import it.unina.bugboard.bugboard_backend.security.JwtService;
import it.unina.bugboard.bugboard_backend.security.SecurityConstants;
import it.unina.bugboard.bugboard_backend.service.AuthService;

import jakarta.servlet.http.Cookie;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
            includeFilters = @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))

@Import(SecurityConfig.class)
@AutoConfigureJsonTesters
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    AuthService authService;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @MockitoBean
    CustomAccessDeniedHandler customAccessDeniedHandler;

    private final String apiUrl = "/api/auth";



    @Test
    void login_validCredentials_Returns200AndSetsCookie() throws Exception {
        AuthRequest request = new AuthRequest("simone@test.com", "secret123");

        AuthResult fakeResult = new AuthResult(
                "fake.jwt.token",
                "simone",
                "simone@test.com",
                Role.TECHNICAL
        );

        when(authService.login(any(AuthRequest.class))).thenReturn(fakeResult);

        mockMvc.perform(
                post(apiUrl + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("simone"))
                .andExpect(jsonPath("$.email").value("simone@test.com"))
                .andExpect(jsonPath("$.role").value("TECHNICAL"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString(SecurityConstants.JWT_COOKIE_NAME + "=fake.jwt.token")));
    }

    @Test
    void logout_validRequest_Returns200AndClearsCookie() throws Exception {
        mockMvc.perform(
                post(apiUrl + "/logout")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString(SecurityConstants.JWT_COOKIE_NAME + "=;")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Max-Age=0")));
    }


    @Test
    void me_withValidJwt_Returns200AndUserInfo() throws Exception {
    UUID userId = UUID.randomUUID();
    String fakeToken = "fake.jwt.token";
    
    User fakeUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("testuser@example.com")
            .role(Role.TECHNICAL)
            .build();

    when(jwtService.extractUserId(fakeToken)).thenReturn(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));
    when(jwtService.isTokenValid(fakeToken, userId)).thenReturn(true);

    mockMvc.perform(
        get(apiUrl + "/me")
            .cookie(new Cookie(SecurityConstants.JWT_COOKIE_NAME, fakeToken))
    )
            .andDo(print())
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.username").value("testuser"))
    .andExpect(jsonPath("$.email").value("testuser@example.com"));
    }
}