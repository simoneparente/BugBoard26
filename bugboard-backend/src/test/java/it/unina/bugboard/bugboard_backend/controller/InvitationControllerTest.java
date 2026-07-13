package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.CreateInvitationRequest;
import it.unina.bugboard.bugboard_backend.dto.InvitationResponse;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.service.InvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationControllerTest {

    @Mock
    private InvitationService invitationService;

    @InjectMocks
    private InvitationController invitationController;

    private LocalDateTime expirationTime;

    @BeforeEach
    void setUp() {
        expirationTime = LocalDateTime.now().plusHours(24);
    }

    @Test
    void createInvitation_WithValidTechnicalRole_ReturnsCreatedResponse() {
        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .role(Role.TECHNICAL)
                .build();

        InvitationResponse expectedResponse = new InvitationResponse(
                "test-token-123",
                Role.TECHNICAL,
                expirationTime
        );

        when(invitationService.createInvitation(Role.TECHNICAL)).thenReturn(expectedResponse);

        ResponseEntity<InvitationResponse> response = invitationController.createInvitation(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-token-123", response.getBody().getToken());
        assertEquals(Role.TECHNICAL, response.getBody().getRole());
        
        verify(invitationService, times(1)).createInvitation(Role.TECHNICAL);
    }

    @Test
    void createInvitation_WithAdminRole_ReturnsCreatedResponse() {
        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .role(Role.ADMIN)
                .build();

        InvitationResponse expectedResponse = new InvitationResponse(
                "admin-token-456",
                Role.ADMIN,
                expirationTime
        );

        when(invitationService.createInvitation(Role.ADMIN)).thenReturn(expectedResponse);

        ResponseEntity<InvitationResponse> response = invitationController.createInvitation(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Role.ADMIN, response.getBody().getRole());
        
        verify(invitationService, times(1)).createInvitation(Role.ADMIN);
    }

    @Test
    void createInvitation_WithExternalRole_ReturnsCreatedResponse() {
        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .role(Role.EXTERNAL)
                .build();

        InvitationResponse expectedResponse = new InvitationResponse(
                "external-token-789",
                Role.EXTERNAL,
                expirationTime
        );

        when(invitationService.createInvitation(Role.EXTERNAL)).thenReturn(expectedResponse);

        ResponseEntity<InvitationResponse> response = invitationController.createInvitation(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Role.EXTERNAL, response.getBody().getRole());
        
        verify(invitationService, times(1)).createInvitation(Role.EXTERNAL);
    }

    @Test
    void createInvitation_VerifiesServiceCall() {
        CreateInvitationRequest request = CreateInvitationRequest.builder()
                .role(Role.TECHNICAL)
                .build();

        InvitationResponse expectedResponse = new InvitationResponse(
                "token",
                Role.TECHNICAL,
                expirationTime
        );

        when(invitationService.createInvitation(any(Role.class))).thenReturn(expectedResponse);

        invitationController.createInvitation(request);

        verify(invitationService).createInvitation(request.getRole());
    }
}
