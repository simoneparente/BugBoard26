package it.unina.bugboard.bugboard_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagRequest {

    @NotBlank(message = "Tag name is required")
    private String name;

    @NotBlank(message = "Tag color is required")
    private String color;

    @NotBlank(message = "Project Key is required")
    private String projectKey;
}