package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.InvitationResponse;
import it.unina.bugboard.bugboard_backend.entity.Invitation;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {
    private static final Logger log = LoggerFactory.getLogger(InvitationService.class);
    private final InvitationRepository invitationRepository;
    private static final int INVITATION_EXPIRATION_HOURS = 24;
    
    private static final int CLEANUP_INTERVAL_HOURS = 1;
    private static final long ONE_HOUR_MILLISECONDS = 3600000;

    public InvitationResponse createInvitation(Role role) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now(ZoneId.of("UTC")).plusHours(INVITATION_EXPIRATION_HOURS);

        Invitation invitation = Invitation.builder()
                .token(token)
                .role(role)
                .expiresAt(expiry)
                .build();

        invitationRepository.save(invitation);
        return new InvitationResponse(token, role, expiry);
    }

    @Scheduled(fixedDelay = CLEANUP_INTERVAL_HOURS * ONE_HOUR_MILLISECONDS)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanupExpiredInvitations() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        long deletedCount = invitationRepository.deleteByExpiresAtBefore(now);
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired invitations", deletedCount);
        }
    }
}
