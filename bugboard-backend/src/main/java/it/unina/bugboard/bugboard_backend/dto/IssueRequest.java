package it.unina.bugboard.bugboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.UUID;

import it.unina.bugboard.bugboard_backend.entity.IssueType;
import it.unina.bugboard.bugboard_backend.entity.IssuePriority;
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
    @NotBlank(message="Project ID is required")
    @UUID(message="Project ID must be a valid UUID")
    private java.util.UUID projectId;
    private StatusResponse status;
    private IssueType type;
    private IssuePriority priority;
    private List<java.util.UUID> tagIds;
}
