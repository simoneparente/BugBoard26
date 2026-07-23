package it.unina.bugboard.bugboard_backend.mapper;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import lombok.AllArgsConstructor;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class IssueMapper {

    private final TagMapper tagMapper;

    public IssueResponse toResponse(Issue issue) {
        if (issue == null) {
            return null;
        }

        return IssueResponse.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .status(issue.getStatus().toString())
                .priority(issue.getPriority().toString())
                .type(issue.getType().toString())
                .assigneeUsername(getAssigneeUsername(issue))
                .tags(mapTags(issue))
                .projectId(issue.getProject().getId())
                .projectName(issue.getProject().getName())
                .build();
    }

    private String getAssigneeUsername(Issue issue) {
        return issue.getAssignee() != null && issue.getAssignee().getUsername() != null 
                ? issue.getAssignee().getUsername() 
                : "Unassigned";
    }

    private List<TagResponse> mapTags(Issue issue) {
        if (issue.getTags() == null || issue.getTags().isEmpty()) {
            return List.of();
        }
        return issue.getTags().stream()
                .map(tagMapper::toResponse)
                .toList();
    }
}

