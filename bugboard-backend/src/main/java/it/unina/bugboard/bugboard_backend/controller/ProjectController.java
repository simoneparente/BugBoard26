package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
import it.unina.bugboard.bugboard_backend.dto.ProjectResponse;
import it.unina.bugboard.bugboard_backend.service.ProjectService;
import jakarta.validation.Valid;

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
    public ResponseEntity<ProjectResponse> createProject(@Valid@RequestBody ProjectRequest projectRequest) {
        ProjectResponse project = projectService.createProject(projectRequest);
        return new ResponseEntity<>(project, HttpStatus.CREATED);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        ProjectResponse project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }
}
