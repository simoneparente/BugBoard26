package it.unina.bugboard.bugboard_backend.security;

import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private Cookie createJwtCookie(String value) {
        Cookie cookie = mock(Cookie.class);
        when(cookie.getName()).thenReturn(SecurityConstants.JWT_COOKIE_NAME);
        when(cookie.getValue()).thenReturn(value);
        return cookie;
    }

    @Test
    void doFilterInternal_NoCookies_ContinuesChainWithoutAuthentication() throws Exception {
        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_NoCookieMatches_ContinuesChainWithoutAuthentication() throws Exception {
        Cookie otherCookie = mock(Cookie.class);
        when(otherCookie.getName()).thenReturn("OTHER_COOKIE");
        when(request.getCookies()).thenReturn(new Cookie[]{otherCookie});

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilterInternal_InvalidJwt_ContinuesChainWithoutAuthentication() throws Exception {
        Cookie jwtCookie = createJwtCookie("invalid-token");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtService.extractUserId("invalid-token")).thenThrow(new RuntimeException("malformed"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_UserNotFound_ContinuesChainWithoutAuthentication() throws Exception {
        UUID userId = UUID.randomUUID();
        Cookie jwtCookie = createJwtCookie("token");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtService.extractUserId("token")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidTokenForUser_DoesNotAuthenticate() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("u").email("u@e.com")
                .passwordHash("h").role(Role.TECHNICAL).build();
        Cookie jwtCookie = createJwtCookie("token");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtService.extractUserId("token")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("token", userId)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidToken_SetsAuthenticationInContext() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("admin").email("a@e.com")
                .passwordHash("h").role(Role.ADMIN).build();
        Cookie jwtCookie = createJwtCookie("good-token");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtService.extractUserId("good-token")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("good-token", userId)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(userId.toString(), auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_AlreadyAuthenticated_DoesNotOverride() throws Exception {
        UUID userId = UUID.randomUUID();
        Authentication existing = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existing);
        Cookie jwtCookie = createJwtCookie("token");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtService.extractUserId("token")).thenReturn(userId);

        filter.doFilterInternal(request, response, filterChain);

        assertSame(existing, SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verify(userRepository, never()).findById(any());
    }
}
