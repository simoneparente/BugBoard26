package it.unina.bugboard.bugboard_backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class IssueResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String projectName;
    private String statusName;
    private String statusColor;
    private String creatorUsername;
    private String assigneeUsername; //  "Unassigned" se nullo
    
    // Liste semplificate di informazioni
    private List<String> tagNames;
    private int attachmentsCount;
}
