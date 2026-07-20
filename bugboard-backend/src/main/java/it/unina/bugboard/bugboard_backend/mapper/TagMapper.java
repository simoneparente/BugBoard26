package it.unina.bugboard.bugboard_backend.mapper;

import org.springframework.stereotype.Component;
import lombok.AllArgsConstructor;

import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Tag;
import it.unina.bugboard.bugboard_backend.service.ProjectService;

@Component
@AllArgsConstructor
public class TagMapper {
    private ProjectService projectService;
    
    public Tag mapToEntity(TagResponse tagResponse) {
        Project project = projectService.getProjectById(tagResponse.getProjectId());
        return Tag.builder()
                .id(tagResponse.getId())
                .name(tagResponse.getName())
                .color(tagResponse.getColor())
                .project(project)
                .build();
    }

    public TagResponse toResponse(Tag tag) {
        if (tag == null) {
            return null;
        }

        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .projectId(tag.getProject() != null ? tag.getProject().getId() : null)
                .build();
    }
}