package it.unina.bugboard.bugboard_backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import it.unina.bugboard.bugboard_backend.entity.Issue;

@Data
@Builder
public class ProjectResponse {
    //returns project data 
    
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private int issueCount; 
    private List<IssueResponse> issueCollection;
}
