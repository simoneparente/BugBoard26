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
import java.time.Clock;
import java.time.Instant;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private InvitationService invitationService;

    @Test
    void createInvitation_Success_SavesAndReturnsCorrectData() {
        Role role = Role.ADMIN;

        LocalDateTime fixedNow = LocalDateTime.of(2026, Month.JUNE, 23, 12, 0, 0);
        Instant fixedInstant = fixedNow.atZone(ZoneId.of("UTC")).toInstant();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        InvitationResponse response = invitationService.createInvitation(role);

        assertNotNull(response.getToken());
        assertEquals(role, response.getRole());

        // About 24 hours from now, with a 2 minutes margin to account for execution time
        LocalDateTime expected = fixedNow.plusHours(24);
        assertEquals(expected, response.getExpiresAt());

        // Verify that the invitation was saved with the correct data
        ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepository).save(invitationCaptor.capture());

        Invitation savedInvitation = invitationCaptor.getValue();
        assertEquals(response.getToken(), savedInvitation.getToken());
        assertEquals(role, savedInvitation.getRole());
    }

    @Test
    void cleanupExpiredInvitations_WithDeletedCountGreaterThanZero_LogsMessage() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, Month.JUNE, 23, 12, 0, 0);
        Instant fixedInstant = fixedNow.atZone(ZoneId.of("UTC")).toInstant();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        when(invitationRepository.deleteByExpiresAtBefore(fixedNow)).thenReturn(5L);

        invitationService.cleanupExpiredInvitations();
        verify(invitationRepository).deleteByExpiresAtBefore(fixedNow);
    }

    @Test
    void cleanupExpiredInvitations_WithDeletedCountZero_DoesNotLogMessage() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, Month.JUNE, 23, 12, 0, 0);
        Instant fixedInstant = fixedNow.atZone(ZoneId.of("UTC")).toInstant();
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        when(invitationRepository.deleteByExpiresAtBefore(fixedNow)).thenReturn(0L);

        invitationService.cleanupExpiredInvitations();
        verify(invitationRepository).deleteByExpiresAtBefore(fixedNow);
    }
}