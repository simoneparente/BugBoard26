package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.AddProjectMembersRequest;
import it.unina.bugboard.bugboard_backend.dto.AssigneeRecommendationResponse;
import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
import it.unina.bugboard.bugboard_backend.dto.ProjectResponse;
import it.unina.bugboard.bugboard_backend.dto.UserResponse;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.mapper.ProjectMapper;
import it.unina.bugboard.bugboard_backend.mapper.UserMapper;
import it.unina.bugboard.bugboard_backend.service.ProjectService;
import jakarta.validation.Valid;
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
    private final UserMapper userMapper;

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

    @PostMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectResponse> addMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        Project project = projectService.addMemberToProject(projectId, userId);
        return ResponseEntity.ok(projectMapper.toResponse(project));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectResponse> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        Project project = projectService.removeMemberFromProject(projectId, userId);
        return ResponseEntity.ok(projectMapper.toResponse(project));
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<Page<UserResponse>> getProjectMembers(
            @PathVariable UUID projectId,
            Pageable pageable) {
        Page<User> members = projectService.getProjectMembers(projectId, pageable);
        return ResponseEntity.ok(members.map(userMapper::toResponse));
    }

    @GetMapping("/{projectId}/available-users")
    public ResponseEntity<Page<UserResponse>> getAvailableUsers(
            @PathVariable UUID projectId,
            Pageable pageable) {
        Page<User> availableUsers = projectService.getAvailableUsers(projectId, pageable);
        return ResponseEntity.ok(availableUsers.map(userMapper::toResponse));
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<List<UserResponse>> addMembersToProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMembersRequest request) {
        List<User> addedMembers = projectService.addMembersToProject(projectId, request.getUserIds());
        return new ResponseEntity<>(
                addedMembers.stream()
                        .map(userMapper::toResponse)
                        .toList(),
                HttpStatus.CREATED);
    }
}
