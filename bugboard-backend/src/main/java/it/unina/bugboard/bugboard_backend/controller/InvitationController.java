package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.CreateInvitationRequest;
import it.unina.bugboard.bugboard_backend.dto.InvitationResponse;
import it.unina.bugboard.bugboard_backend.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {
    private final InvitationService invitationService;

    @PostMapping
    public ResponseEntity<InvitationResponse> createInvitation(@Valid @RequestBody CreateInvitationRequest request) {
        InvitationResponse invitationResponse = invitationService.createInvitation(request.getRole());
        return ResponseEntity.ok(invitationResponse);
    }
}
