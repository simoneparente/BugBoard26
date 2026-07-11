package it.unina.bugboard.bugboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import it.unina.bugboard.bugboard_backend.entity.IssueType;
import it.unina.bugboard.bugboard_backend.entity.IssuePriority;
import it.unina.bugboard.bugboard_backend.entity.IssueStatus;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueRequest {
    @NotBlank(message="Title is required")
    private String title;
    @NotBlank(message="Description is required")
    private String description;
    
    private String assigneeUsername; // Optional, can be null
    private IssueStatus status;
    private IssueType type;
    private IssuePriority priority;
    private List<TagResponse> tags;
    private List<AttachmentMetadataRequest> attachments;
}
