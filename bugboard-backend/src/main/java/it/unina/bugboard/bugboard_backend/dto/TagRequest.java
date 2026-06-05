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

    @NotBlank(message = "Il nome del tag è obbligatorio")
    private String name;

    @NotBlank(message = "Il colore del tag è obbligatorio")
    private String color;

    @NotNull(message = "L'ID del progetto è obbligatorio")
    private UUID projectId;
}