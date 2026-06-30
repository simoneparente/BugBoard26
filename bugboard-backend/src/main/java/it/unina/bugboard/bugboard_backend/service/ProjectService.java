package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
import it.unina.bugboard.bugboard_backend.dto.ProjectResponse;
import it.unina.bugboard.bugboard_backend.dto.StatusResponse;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.dto.UserResponse;
import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest projectrequest){
        if(projectRepository.existsByName(projectrequest.getName())){
            throw new IllegalArgumentException("Project with the same name already exists.");
        }
        Project project = Project.builder()
                .name(projectrequest.getName())
                .description(projectrequest.getDescription())
                .build();
        projectRepository.save(project);
        return mapToResponse(project);
    }   

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id){
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found."));
        return mapToResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(){
        List<Project> projects = projectRepository.findAll();
        return projects.stream().map(this::mapToResponse).toList();
    }

    private ProjectResponse mapToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getIssues().stream().map(this::mapToIssueResponse).toList(),
                project.getMembers().stream().map(this::mapToUserResponse).toList()
        );
    }

    public IssueResponse mapToIssueResponse(Issue issue) {
        return new IssueResponse(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getProject().getName(),
                new StatusResponse(issue.getStatus().getName(), issue.getStatus().getClass().getSimpleName()),
                issue.getCreator() != null ? issue.getCreator().getUsername() : "Unknown",
                issue.getAssignee() != null ? issue.getAssignee().getUsername() : "Unassigned",
                issue.getTags().stream().map(this::mapToTagResponse).toList(),
                issue.getAttachments().size()
        );
    }


    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    private TagResponse mapToTagResponse(it.unina.bugboard.bugboard_backend.entity.Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getColor(),
                tag.getProject().getId()
        );
    }

}
