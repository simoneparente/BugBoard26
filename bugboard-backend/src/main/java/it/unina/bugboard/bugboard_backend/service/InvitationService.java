package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.InvitationResponse;
import it.unina.bugboard.bugboard_backend.entity.Invitation;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {
    private final InvitationRepository invitationRepository;
    private static final int INVITATION_EXPIRATION_HOURS = 24;

    public InvitationResponse createInvitation(Role role) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(INVITATION_EXPIRATION_HOURS);

        Invitation invitation = Invitation.builder()
                .token(token)
                .role(role)
                .expiresAt(expiry)
                .build();

        invitationRepository.save(invitation);
        return new InvitationResponse(token, role, expiry);
    }
}
