package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.ProjectResponse;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;

import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * Returns all projects for admins, or only the projects the caller belongs to for other roles.
     */
    public Page<ProjectResponse> getAllProjects(UUID callerId, Pageable pageable) {
        User caller = userRepository.findById(callerId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with id %s not found", callerId)));

        if (caller.getRole() == Role.ADMIN) {
            return projectRepository.findAll(pageable).map(this::mapToResponse);
        }
        return projectRepository.findByUsersContaining(caller, pageable).map(this::mapToResponse);
    }

    public ProjectResponse createProject(ProjectRequest request) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        projectRepository.save(project);
        return mapToResponse(project);
    }

    public ProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Project with id %s not found", id)));
        return mapToResponse(project);
    }

    public void deleteProject(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException(String.format("Project with id %s not found", id));
        }
        projectRepository.deleteById(id);
    }


    private ProjectResponse mapToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getIssueCount() != null ? project.getIssueCount() : 0
        );
    }
}