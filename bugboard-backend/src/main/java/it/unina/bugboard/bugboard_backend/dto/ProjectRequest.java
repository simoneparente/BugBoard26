package it.unina.bugboard.bugboard_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {
    @NotBlank(message = "Project name is required")
    private String name;
    @NotBlank(message = "Project description is required")
    private String description;

}

