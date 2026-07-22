package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import jakarta.annotation.Resource;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    private static final String PROJECT_NOT_FOUND_MESSAGE = "Project not found.";

    @Resource
    private final ObjectProvider<ProjectService> selfProvider;

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
                    .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_MESSAGE));
        }
        
        // TECHNICAL and EXTERNAL can only see projects they are members of
        if (!projectRepository.existsByIdAndMembersId(id, currentUser.getId())) {
            throw new UnauthorizedException("You don't have access to this project.");
        }
        
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_MESSAGE));
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

    @Transactional(readOnly = true)
    public Page<User> getProjectMembers(UUID projectId, Pageable pageable) {
        Project project = selfProvider.getObject().getProjectById(projectId);
        List<User> members = project.getMembers();
        
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        int start = pageNumber * pageSize;
        int end = Math.min(start + pageSize, members.size());
        
        List<User> pageContent = members.subList(start, end);
        return new PageImpl<>(pageContent, pageable, members.size());
    }

    @Transactional(readOnly = true)
    public Page<User> getAvailableUsers(UUID projectId, Pageable pageable) {
        Project project = selfProvider.getObject().getProjectById(projectId);
        Set<UUID> memberIds = project.getMembers().stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        
        Page<User> allUsers = userRepository.findAll(pageable);
        List<User> availableUsers = allUsers.stream()
                .filter(user -> !memberIds.contains(user.getId()))
                // ADMIN can only add TECHNICAL and EXTERNAL users to projects
                .filter(user -> user.getRole() != Role.ADMIN)
                .toList();
        
        return new PageImpl<>(availableUsers, pageable, availableUsers.size());
    }

    @Transactional
    public List<User> addMembersToProject(UUID projectId, List<UUID> userIds) {
        User currentUser = getCurrentUser();
        
        // Only ADMIN can add members to projects
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can add members to projects.");
        }
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_MESSAGE));
        
        Set<UUID> currentMemberIds = project.getMembers().stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        
        List<User> usersToAdd = userRepository.findAllById(userIds);
        
        if (usersToAdd.size() != userIds.size()) {
            throw new ResourceNotFoundException("One or more users not found.");
        }
        
        // ADMIN can only add TECHNICAL and EXTERNAL users to projects, not other ADMIN users
        boolean hasAdminUsers = usersToAdd.stream()
                .anyMatch(user -> user.getRole() == Role.ADMIN);
        
        if (hasAdminUsers) {
            throw new UnauthorizedException("ADMIN users cannot be added as project members. Only TECHNICAL and EXTERNAL users are allowed.");
        }
        
        List<User> newMembers = usersToAdd.stream()
                .filter(user -> !currentMemberIds.contains(user.getId()))
                .toList();
        
        if (newMembers.isEmpty()) {
            throw new IllegalArgumentException("All specified users are already members of this project.");
        }
        
        project.getMembers().addAll(newMembers);
        projectRepository.save(project);
        
        return newMembers;
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