package it.unina.bugboard.bugboard_backend.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProjectMembersRequest {
    @NotEmpty(message = "At least one user ID is required")
    private List<UUID> userIds;
}
