package it.unina.bugboard.bugboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String projectName;

    private StatusResponse status;

    private String creatorUsername;
    private String assigneeUsername; //  "Unassigned" if null
    
    // Simplified lists of information
    private List<TagResponse> tags;
    private int attachmentsCount;
}
