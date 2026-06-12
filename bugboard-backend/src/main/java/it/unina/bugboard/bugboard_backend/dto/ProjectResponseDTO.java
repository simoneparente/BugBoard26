package it.unina.bugboard.bugboard_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProjectResponseDTO {
    //restituisce i dati del progetto 
    
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private int issueCount; 
}
