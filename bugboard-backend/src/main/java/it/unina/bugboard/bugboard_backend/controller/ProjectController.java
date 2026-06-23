package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.ProjectRequestDTO;
import it.unina.bugboard.bugboard_backend.dto.ProjectResponseDTO;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping 
    public ResponseEntity<ProjectResponseDTO> createProject(@RequestBody ProjectRequestDTO projectRequest) {
        Project project = projectService.createProject(projectRequest.getName(), projectRequest.getDescription());
        return new ResponseEntity<>(mapToResponseDTO(project), HttpStatus.CREATED);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable UUID id) {
        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(mapToResponseDTO(project));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects() {
        List<ProjectResponseDTO> projects = projectService.getAllProjects().stream()
                .map(this::mapToResponseDTO)
                .toList();
        return ResponseEntity.ok(projects);
    }

    private ProjectResponseDTO mapToResponseDTO(Project project) {
        return ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .issueCount(project.getIssues() != null ? project.getIssues().size() : 0)
                .build();
    }
    
}
