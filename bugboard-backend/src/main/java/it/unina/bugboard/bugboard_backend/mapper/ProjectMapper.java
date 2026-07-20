package it.unina.bugboard.bugboard_backend.mapper;

import it.unina.bugboard.bugboard_backend.dto.ProjectResponse;
import it.unina.bugboard.bugboard_backend.entity.Project;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectMapper {

    private final IssueMapper issueMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;

    public ProjectMapper(IssueMapper issueMapper, TagMapper tagMapper, UserMapper userMapper) {
        this.issueMapper = issueMapper;
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
    }

    public ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .issues(project.getIssues() != null ?
                        project.getIssues().stream()
                                .map(issueMapper::toResponse)
                                .toList()
                        : List.of())
                .tags(project.getTags() != null ?
                        project.getTags().stream()
                                .map(tagMapper::toResponse)
                                .toList()
                        : List.of())
                .members(project.getMembers() != null ?
                        project.getMembers().stream()
                                .map(userMapper::toResponse)
                                .toList()
                        : List.of())
                .build();
    }
}
