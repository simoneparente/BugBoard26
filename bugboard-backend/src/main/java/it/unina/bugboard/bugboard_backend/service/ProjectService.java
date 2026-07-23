package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.exception.UnauthorizedException;
import it.unina.bugboard.bugboard_backend.dto.AssigneeRecommendationResponse;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.IssueStatus;
import it.unina.bugboard.bugboard_backend.mapper.UserMapper;
import it.unina.bugboard.bugboard_backend.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final UserMapper userMapper;

    private static final String PROJECT_NOT_FOUND_MESSAGE = "Project not found.";

    /**
     * Creates a new project with the current user as the initial member.
     *
     * @param projectRequest project creation request
     * @return the created project
     * @throws IllegalArgumentException if project name already exists
     */
    @Transactional
    public Project createProject(ProjectRequest projectRequest) {
        User currentUser = getCurrentUser();

        if (projectRepository.existsByName(projectRequest.getName())) {
            throw new IllegalArgumentException("Project with the same name already exists.");
        }

        Project project = Project.builder()
                .name(projectRequest.getName())
                .description(projectRequest.getDescription())
                .members(List.of(currentUser)) 
                .build();
                
        return projectRepository.save(project);
    }   

    /**
     * Retrieves a project by ID with access control.
     * ADMIN can see all projects; others can only see projects they belong to.
     *
     * @param id project ID
     * @return the project
     * @throws ResourceNotFoundException if project not found
     * @throws UnauthorizedException if user lacks access
     */
    @Transactional(readOnly = true)
    public Project getProjectById(UUID id) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.ADMIN) {
            return projectRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_MESSAGE));
        }
        
        if (!projectRepository.existsByIdAndMembersId(id, currentUser.getId())) {
            throw new UnauthorizedException("You don't have access to this project.");
        }
        
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_MESSAGE));
    }

    /**
     * Retrieves all projects accessible to the current user with pagination.
     * ADMIN sees all projects; others see only their projects.
     *
     * @param pageable pagination information
     * @return page of projects
     */
    @Transactional(readOnly = true)
    public Page<Project> getProjects(Pageable pageable) {
        User currentUser = getCurrentUser();
        
        if (currentUser.getRole() == Role.ADMIN) {
            return projectRepository.findAll(pageable);
        }
        
        return projectRepository.findByMembersId(currentUser.getId(), pageable);
    }

    /**
     * Deletes a project (ADMIN only).
     *
     * @param id project ID
     * @throws UnauthorizedException if user is not ADMIN
     */
    @Transactional
    public void deleteProject(UUID id) {
        User currentUser = getCurrentUser();
        validateUserIsAdmin(currentUser);
        projectRepository.deleteById(id);
    }

    @Transactional
    public Project addMemberToProject(UUID projectId, UUID userId) {
        Project project = getProjectById(projectId);
        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!project.getMembers().contains(userToAdd)) {
            project.getMembers().add(userToAdd);
        }

        return projectRepository.save(project);
    }

    @Transactional
    public Project removeMemberFromProject(UUID projectId, UUID userId) {
        Project project = getProjectById(projectId);
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        project.getMembers().remove(userToRemove);
        return projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<AssigneeRecommendationResponse> getRecommendedAssignees(UUID projectId) {
        Project project = projectRepository.findByIdWithMembers(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        List<User> members = project.getMembers();
        if (members == null || members.isEmpty()) {
            return List.of();
        }

        List<Issue> activeIssues = issueRepository.findByProjectIdAndStatusNotIn(
                projectId, List.of(IssueStatus.COMPLETED, IssueStatus.CLOSED));

        Map<UUID, List<Issue>> issuesByAssignee = activeIssues.stream()
                .filter(issue -> issue.getAssignee() != null)
                .collect(Collectors.groupingBy(issue -> issue.getAssignee().getId()));

        return members.stream()
                .filter(user -> user.getRole() != Role.ADMIN && user.getRole() != Role.EXTERNAL)
                .map(user -> {
                    List<Issue> userIssues = issuesByAssignee.getOrDefault(user.getId(), List.of());
                    int workloadScore = userIssues.stream()
                            .mapToInt(issue -> issue.getPriority() != null ? issue.getPriority().getWeight() : 1)
                            .sum();
                    return AssigneeRecommendationResponse.builder()
                            .user(userMapper.toResponse(user))
                            .workloadScore(workloadScore)
                            .activeIssueCount(userIssues.size())
                            .build();
                })
                .sorted(Comparator.comparingInt(AssigneeRecommendationResponse::getWorkloadScore)
                        .thenComparingInt(AssigneeRecommendationResponse::getActiveIssueCount)
                        .thenComparing(res -> res.getUser() != null && res.getUser().getUsername() != null ? res.getUser().getUsername() : ""))
                .limit(3)
                .toList();
    }

    /**
     * Retrieves paginated members of a project.
     * Delegates database pagination to the repository.
     *
     * @param projectId project ID
     * @param pageable pagination information
     * @return page of project members
     * @throws ResourceNotFoundException if project not found
     */
    @Transactional(readOnly = true)
    public Page<User> getProjectMembers(UUID projectId, Pageable pageable) {
        getAndValidateProject(projectId);
        return projectRepository.findMembersByProjectId(projectId, pageable);
    }

    /**
     * Retrieves available users (non-ADMIN, not yet members) for a project.
     * Delegates database pagination and filtering to the repository.
     *
     * @param projectId project ID
     * @param pageable pagination information
     * @return page of available users
     * @throws ResourceNotFoundException if project not found
     */
    @Transactional(readOnly = true)
    public Page<User> getAvailableUsers(UUID projectId, Pageable pageable) {
        getAndValidateProject(projectId);
        return projectRepository.findAvailableUsersForProject(projectId, pageable);
    }

    /**
     * Adds members to a project (ADMIN only).
     * Validates that users are not already members and are not ADMIN users.
     *
     * @param projectId project ID
     * @param userIds user IDs to add
     * @return list of newly added members
     * @throws UnauthorizedException if user is not ADMIN
     * @throws ResourceNotFoundException if project or users not found
     * @throws IllegalArgumentException if all users are already members
     */
    @Transactional
    public List<User> addMembersToProject(UUID projectId, List<UUID> userIds) {
        User currentUser = getCurrentUser();
        validateUserIsAdmin(currentUser);
        
        Project project = getAndValidateProject(projectId);
        
        Set<UUID> currentMemberIds = project.getMembers().stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        
        List<User> usersToAdd = userRepository.findAllById(userIds);
        
        if (usersToAdd.size() != userIds.size()) {
            throw new ResourceNotFoundException("One or more users not found.");
        }
        
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

    // ============ PRIVATE AUTHORIZATION & UTILITY METHODS ============

    /**
     * Validates that the user has ADMIN role.
     *
     * @param user the user to validate
     * @throws UnauthorizedException if user is not ADMIN
     */
    private void validateUserIsAdmin(User user) {
        if (user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can perform this operation.");
        }
    }

    /**
     * Retrieves and validates a project exists.
     *
     * @param projectId project ID
     * @return the project
     * @throws ResourceNotFoundException if project not found
     */
    private Project getAndValidateProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND_MESSAGE));
    }

    /**
     * Retrieves the current authenticated user.
     *
     * @return the current user
     * @throws UnauthorizedException if user not authenticated or not found
     */
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