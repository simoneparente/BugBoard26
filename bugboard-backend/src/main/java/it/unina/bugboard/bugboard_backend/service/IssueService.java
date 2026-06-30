package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.IssueRequest;
import it.unina.bugboard.bugboard_backend.entity.*;
import it.unina.bugboard.bugboard_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;


@Service
public class IssueService {

    private static final String ISSUE_NOT_FOUND_MSG = "Issue not found with ID: ";

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public IssueService(IssueRepository issueRepository, UserRepository userRepository, 
                        ProjectRepository projectRepository, TagRepository tagRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.tagRepository = tagRepository;
    }

   @Transactional
    public Issue createIssue(IssueRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found."));

       List<Tag> tags = (request.getTagIds() != null && !request.getTagIds().isEmpty())
                ? tagRepository.findAllById(request.getTagIds())
                : List.of();

        Issue issue = Issue.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .priority(request.getPriority())
                .type(request.getType())
                .tags(tags)
                .createdAt(LocalDateTime.now(ZoneId.systemDefault())) 
                .updatedAt(LocalDateTime.now(ZoneId.systemDefault()))
                .build();

        return issueRepository.save(issue);
    }

    @Transactional
    public Issue assignIssue(UUID issueId, UUID assigneeId){
        Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));
        User assignee = userRepository.findById(assigneeId).orElseThrow(() -> new RuntimeException("Assignee not found with ID: " + assigneeId));
        
        issue.setAssignee(assignee);  
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue startIssueProgress(UUID issueId){
        Issue issue = issueRepository.findById(issueId)
                        .orElseThrow(()-> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));

        issue.startPorgress();
        return issueRepository.save(issue);
    }

    @Transactional 
    public Issue acceptIssue(UUID issueId){
        Issue issue = issueRepository.findById(issueId)
                        .orElseThrow(()-> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));
        
        issue.accept();
        return issueRepository.save(issue);
    }   

    @Transactional
    public Issue rollbackIssueState(UUID issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));
        
        issue.previousState();
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue removeIssueAssignee(UUID issueId){
        Issue issue = issueRepository.findById(issueId)
                        .orElseThrow(()-> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));

        issue.removeAssignee();
        return issueRepository.save(issue);
    }

 
    @Transactional(readOnly = true)
    public Issue getIssueById(UUID id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(ISSUE_NOT_FOUND_MSG + id));
    }


    @Transactional(readOnly = true)
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    @Transactional
    public void deleteIssue(UUID id) {
        if (!issueRepository.existsById(id)) {
            throw new IllegalArgumentException("Cannot delete. Issue not found with ID: " + id);
        }
        issueRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Issue> getIssuesByProjectId(UUID projectId) {
        return issueRepository.findByProjectId(projectId);
    }


    @Transactional
    public Issue addTagToIssue(UUID issueId, UUID tagId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with ID: " + tagId));

        issue.getTags().add(tag);
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue removeTagFromIssue(UUID issueId, UUID tagId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with ID: " + tagId));

        issue.getTags().remove(tag);
        return issueRepository.save(issue);
    }
}
