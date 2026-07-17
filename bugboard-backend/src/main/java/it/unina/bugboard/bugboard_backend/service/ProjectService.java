package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public Project createProject(ProjectRequest projectrequest) {
        User currentUser = getCurrentUser();

        if (projectRepository.existsByName(projectrequest.getName())) {
            throw new IllegalArgumentException("Project with the same name already exists.");
        }

        Project project = Project.builder()
                .name(projectrequest.getName())
                .description(projectrequest.getDescription())
                .members(List.of(currentUser)) 
                .build();
                
        return projectRepository.save(project);
    }   

    @Transactional(readOnly = true)
    public Project getProjectById(UUID id) {
        User currentUser = getCurrentUser();
        
        // ADMIN can see all projects
        if (currentUser.getRole() == Role.ADMIN) {
            return projectRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
        }
        
        // TECHNICAL and EXTERNAL can only see projects they are members of
        if (!projectRepository.existsByIdAndMembersId(id, currentUser.getId())) {
            throw new UnauthorizedException("You don't have access to this project.");
        }
        
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
    }

    @Transactional(readOnly = true)
    public Page<Project> getProjects(Pageable pageable) {
        User currentUser = getCurrentUser();
        
        // ADMIN can see all projects
        if (currentUser.getRole() == Role.ADMIN) {
            return projectRepository.findAll(pageable);
        }
        
        // TECHNICAL and EXTERNAL can only see projects they are members of
        return projectRepository.findByMembersId(currentUser.getId(), pageable);
    }

    @Transactional
    public void deleteProject(UUID id) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can delete projects.");
        }
        projectRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated.");
        }
        
        String userIdString = authentication.getName();
        UUID userId;
        try {
            userId = UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid user ID format.");
        }
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found."));
    }
}