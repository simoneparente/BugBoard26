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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final UserMapper userMapper;

    @Transactional
    public Project createProject(ProjectRequest projectrequest) {
        User currentUser = getCurrentUser();

        if (projectRepository.existsByName(projectrequest.getName())) {
            throw new IllegalArgumentException("Project with the same name already exists.");
        }

        String baseKey = projectrequest.getName().trim().split("\\s+")[0].toLowerCase().replaceAll("[^a-z]", "");
        if (baseKey.isEmpty()) {
            baseKey = "prj";
        } else if (baseKey.length() > 3) {
            baseKey = baseKey.substring(0, 3);
        }

        String key = baseKey;
        int suffix = 1;
        while (projectRepository.existsByKey(key)) {
            key = baseKey + suffix;
            suffix++;
        }

        Project project = Project.builder()
                .name(projectrequest.getName())
                .key(key)
                .description(projectrequest.getDescription())
                .members(List.of(currentUser)) 
                .build();
                
        return projectRepository.save(project);
    }   

    @Transactional(readOnly = true)
    public Project getProjectByKey(String key) {
        User currentUser = getCurrentUser();
        
        Project project = projectRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));

        // ADMIN can see all projects
        if (currentUser.getRole() == Role.ADMIN) {
            return project;
        }
        
        // TECHNICAL and EXTERNAL can only see projects they are members of
        if (!projectRepository.existsByIdAndMembersId(project.getId(), currentUser.getId())) {
            throw new UnauthorizedException("You don't have access to this project.");
        }
        
        return project;
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
    public void deleteProject(String key) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN users can delete projects.");
        }
        Project project = projectRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));
        projectRepository.deleteById(project.getId());
    }

    @Transactional(readOnly = true)
    public List<AssigneeRecommendationResponse> getRecommendedAssignees(String projectKey) {
        Project p = projectRepository.findByKey(projectKey)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with key: " + projectKey));
        Project project = projectRepository.findByIdWithMembers(p.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + p.getId()));

        List<User> members = project.getMembers();
        if (members == null || members.isEmpty()) {
            return List.of();
        }

        List<Issue> activeIssues = issueRepository.findByProjectIdAndStatusNotIn(
                project.getId(), List.of(IssueStatus.COMPLETED, IssueStatus.CLOSED));

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