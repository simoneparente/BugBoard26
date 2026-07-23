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
    private Long sequenceNumber;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status;
    private String priority;
    private String type;
    private String assigneeUsername; //  "Unassigned" if null
    
    private List<TagResponse> tags;
    private List<AttachmentResponse> attachments;
    
    private UUID projectId;
    private String projectKey;
    private String projectName;
}
