package it.unina.bugboard.bugboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<IssueResponse> issues;
    private List<UserResponse> members;
}