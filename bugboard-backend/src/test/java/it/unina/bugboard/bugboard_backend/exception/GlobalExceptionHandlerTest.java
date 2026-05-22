package it.unina.bugboard.bugboard_backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    private void assertErrorResponse(ResponseEntity<ErrorResponse> response, HttpStatus expectedStatus, String expectedMessage) {
        assertEquals(expectedStatus, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(expectedStatus.value(), body.getStatus());
        assertEquals(expectedStatus.getReasonPhrase(), body.getError());
        assertEquals("/api/test", body.getPath());
        assertNotNull(body.getTimestamp());
        if (expectedMessage != null) {
            assertEquals(expectedMessage, body.getMessage());
        }
    }

    @Test
    void handleResourceNotFoundException_Returns404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFoundException(new ResourceNotFoundException("not found"), request);
        assertErrorResponse(response, HttpStatus.NOT_FOUND, "not found");
    }

    @Test
    void handleValidationException_Returns400WithJoinedMessages() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "field1", "must not be blank"));
        bindingResult.addError(new FieldError("obj", "field2", "must be valid"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("must not be blank"));
        assertTrue(body.getMessage().contains("must be valid"));
        assertTrue(body.getMessage().contains("|"));
    }

    @Test
    void handleInvalidInvitation_Returns403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidInvitation(new InvalidInvitationException("bad token"), request);
        assertErrorResponse(response, HttpStatus.FORBIDDEN, "bad token");
    }

    @Test
    void handleIllegalArgumentException_Returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgumentException(new IllegalArgumentException("bad arg"), request);
        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "bad arg");
    }

    @Test
    void handleBadCredentials_Returns401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBadCredentials(new BadCredentialsException("invalid creds"), request);
        assertErrorResponse(response, HttpStatus.UNAUTHORIZED, "invalid creds");
    }

    @Test
    void handleAuthenticationException_Returns401WithFixedMessage() {
        AuthenticationException ex = new AuthenticationException("internal") {};
        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex, request);
        assertErrorResponse(response, HttpStatus.UNAUTHORIZED, "Unauthorized: Invalid token.");
    }

    @Test
    void handleAccessDenied_Returns403WithFixedMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"), request);
        assertErrorResponse(response, HttpStatus.FORBIDDEN,
                "Forbidden: You don't have permission to access this resource.");
    }

    @Test
    void handleGenericException_Returns500() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(new RuntimeException("boom"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.getMessage().contains("boom"));
    }
}
