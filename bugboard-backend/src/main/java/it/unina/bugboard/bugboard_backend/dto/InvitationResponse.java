package it.unina.bugboard.bugboard_backend.dto;

import it.unina.bugboard.bugboard_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class InvitationResponse {
    private String token;
    private Role role;
    private LocalDateTime expiresAt;
}
