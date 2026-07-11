package it.unina.bugboard.bugboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


import it.unina.bugboard.bugboard_backend.service.ProjectService;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Tag;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {
    private ProjectService projectService; 

    private UUID id;
    private String name;
    private String color;
    private UUID projectId;

    public Tag mapToEntity() {
        Project project = projectService.getProjectById(this.projectId);

        return Tag.builder()
                .id(this.id)
                .name(this.name)
                .color(this.color)
                .project(project)
                .build();
    }
}