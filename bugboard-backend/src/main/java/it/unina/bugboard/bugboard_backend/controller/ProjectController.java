package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
import it.unina.bugboard.bugboard_backend.dto.ProjectResponse;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.mapper.ProjectMapper;
import it.unina.bugboard.bugboard_backend.service.ProjectService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @PostMapping 
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectRequest projectRequest) {
        Project project = projectService.createProject(projectRequest);
        return new ResponseEntity<>(projectMapper.toResponse(project), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(projectMapper.toResponse(project));
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getProjects(Pageable pageable) {
        Page<ProjectResponse> projects = projectService.getProjects(pageable)
                .map(projectMapper::toResponse);
        return ResponseEntity.ok(projects);
    }
}
