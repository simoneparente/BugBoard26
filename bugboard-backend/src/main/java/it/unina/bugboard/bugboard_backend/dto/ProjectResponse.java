package it.unina.bugboard.bugboard_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Data
@Builder
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<IssueResponse> issues;
    private List<TagResponse> tags;
    private List<UserResponse> members;
}
