package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.InvitationResponse;
import it.unina.bugboard.bugboard_backend.entity.Invitation;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.repository.InvitationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @InjectMocks
    private InvitationService invitationService;

    @Test
    void createInvitation_Success_SavesAndReturnsCorrectData() {
        Role role = Role.ADMIN;

        InvitationResponse response = invitationService.createInvitation(role);

        assertNotNull(response.getToken());
        assertEquals(role, response.getRole());

        // About 24 hours from now, with a 2 minutes margin to account for execution time
        assertTrue(response.getExpiresAt().isAfter(LocalDateTime.now(ZoneId.of("UTC")).plusHours(23).plusMinutes(59)));
        assertTrue(response.getExpiresAt().isBefore(LocalDateTime.now(ZoneId.of("UTC")).plusHours(24).plusMinutes(1)));

        // Verify that the invitation was saved with the correct data
        ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(invitationCaptor.capture());

        Invitation savedInvitation = invitationCaptor.getValue();
        assertEquals(response.getToken(), savedInvitation.getToken());
        assertEquals(role, savedInvitation.getRole());
    }
}