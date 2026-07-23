package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.AssigneeRecommendationResponse;
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

import java.util.List;
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

    @GetMapping("/{key}")
    public ResponseEntity<ProjectResponse> getProjectByKey(@PathVariable String key) {
        Project project = projectService.getProjectByKey(key);
        return ResponseEntity.ok(projectMapper.toResponse(project));
    }

    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getProjects(Pageable pageable) {
        Page<ProjectResponse> projects = projectService.getProjects(pageable)
                .map(projectMapper::toResponse);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{key}/recommended-assignees")
    public ResponseEntity<List<AssigneeRecommendationResponse>> getRecommendedAssignees(@PathVariable String key) {
        List<AssigneeRecommendationResponse> recommendations = projectService.getRecommendedAssignees(key);
        return ResponseEntity.ok(recommendations);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteProject(@PathVariable String key) {
        projectService.deleteProject(key);
        return ResponseEntity.noContent().build();
    }
}
