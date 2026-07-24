package it.unina.bugboard.bugboard_backend.mapper;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import org.springframework.stereotype.Component;

@Component
public class IssueMapper {

    public IssueResponse toResponse(Issue issue) {
        if (issue == null) {
            return null;
        }

        return IssueResponse.builder()
                .id(issue.getId())
                .sequenceNumber(issue.getSequenceNumber())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .status(issue.getStatus().toString())
                .priority(issue.getPriority().toString())
                .type(issue.getType().toString())
                .assigneeUsername(getAssigneeUsername(issue))
                .projectId(issue.getProject().getId())
                .projectKey(issue.getProject().getKey())
                .projectName(issue.getProject().getName())
                .build();
    }

    private String getAssigneeUsername(Issue issue) {
        return issue.getAssignee() != null && issue.getAssignee().getUsername() != null ? issue.getAssignee().getUsername() : "Unassigned";
    }
}
