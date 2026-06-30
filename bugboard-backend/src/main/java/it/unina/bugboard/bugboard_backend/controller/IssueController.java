package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.IssueRequest;
import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.dto.StatusResponse;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.service.IssueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@RequestBody IssueRequest request) {
        Issue issue = issueService.createIssue(request);
        return new ResponseEntity<>(mapToResponseDTO(issue), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getIssueById(@PathVariable UUID id) {
        Issue issue = issueService.getIssueById(id);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }


    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<IssueResponse>> getIssuesByProject(@PathVariable UUID projectId) {
        List<IssueResponse> issues = issueService.getIssuesByProjectId(projectId).stream()
                .map(this::mapToResponseDTO)
                .toList();
        return ResponseEntity.ok(issues);
    }


    @PutMapping("/{id}/assign")
    public ResponseEntity<IssueResponse> assignIssue(@PathVariable UUID id, @RequestParam UUID assigneeId) {
        Issue issue = issueService.assignIssue(id, assigneeId);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }


    @PutMapping("/{id}/start-progress")
    public ResponseEntity<IssueResponse> startProgress(@PathVariable UUID id) {
        Issue issue = issueService.startIssueProgress(id);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }


    @PutMapping("/{id}/accept")
    public ResponseEntity<IssueResponse> acceptIssue(@PathVariable UUID id) {
        Issue issue = issueService.acceptIssue(id);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }


    @PutMapping("/{id}/previous")
    public ResponseEntity<IssueResponse> goToPreviousState(@PathVariable UUID id) {
        Issue issue = issueService.rollbackIssueState(id);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }

    @DeleteMapping("/{id}/assignee")
    public ResponseEntity<Issue> removeAssignee(@PathVariable UUID id) {
        Issue updatedIssue = issueService.removeIssueAssignee(id);
        return ResponseEntity.ok(updatedIssue);
    }

    private IssueResponse mapToResponseDTO(Issue issue) {
        StatusResponse statusDTO = null;

        if(issue.getStatus() != null ){
            statusDTO = StatusResponse.builder()
                        .name(issue.getStatus().getName())
                        .type(issue.getStatus().getClass().getSimpleName())
                        .build();
        }

        List<TagResponse> tagDTOs = issue.getTags() != null
            ? issue.getTags().stream()
                    .map(tag -> TagResponse.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .color(tag.getColor())
                            .build())
                    .toList()
            : List.of();

        return IssueResponse.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .projectName(issue.getProject() != null ? issue.getProject().getName() : null)
                .assigneeUsername(issue.getAssignee() != null ? issue.getAssignee().getUsername() : "Unassigned")
                .status(statusDTO)
                .tags(tagDTOs)
                .attachmentsCount(issue.getAttachments() != null ? issue.getAttachments().size() : 0)
                .build();
    }

}
