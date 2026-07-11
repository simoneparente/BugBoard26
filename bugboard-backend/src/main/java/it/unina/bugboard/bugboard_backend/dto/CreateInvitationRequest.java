package it.unina.bugboard.bugboard_backend.dto;

import it.unina.bugboard.bugboard_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvitationRequest {

    @NotNull(message = "Role is required")
    private Role role;

}