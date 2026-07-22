package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.AttachmentResponse;
import it.unina.bugboard.bugboard_backend.dto.IssueRequest;
import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@PathVariable UUID projectId, @Valid @RequestBody IssueRequest request) {
        Issue issue = issueService.createIssue(projectId, request);
        return new ResponseEntity<>(mapToResponseDTO(issue), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponse> getIssueByIdAndProjectId(@PathVariable UUID id, @PathVariable UUID projectId) {
        Issue issue = issueService.getIssueByIdAndProjectId(id, projectId);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }


    @GetMapping
    public ResponseEntity<Page<IssueResponse>> getIssuesByProject(@PathVariable UUID projectId, Pageable pageable) {
        Page<IssueResponse> issues = issueService.getIssuesByProjectId(projectId, pageable)
                .map(this::mapToResponseDTO);
        return ResponseEntity.ok(issues);
    }


    @PutMapping("/{id}/assign")
    public ResponseEntity<IssueResponse> assignIssue(@PathVariable UUID id, @RequestParam UUID assigneeId) {
        Issue issue = issueService.assignIssueToUser(id, assigneeId);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<IssueResponse> setStatus(@PathVariable UUID id, @RequestParam it.unina.bugboard.bugboard_backend.entity.IssueStatus status) {
        Issue issue = issueService.setStatus(id, status);
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
    public ResponseEntity<IssueResponse> removeAssignee(@PathVariable UUID id) {
        Issue updatedIssue = issueService.removeIssueAssignee(id);
        return ResponseEntity.ok(mapToResponseDTO(updatedIssue));
    }

    private IssueResponse mapToResponseDTO(Issue issue) {
        return IssueResponse.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .status(issue.getStatus().name())
                .priority(issue.getPriority().name())
                .type(issue.getType().name())
                .assigneeUsername(issue.getAssignee() != null ? issue.getAssignee().getUsername() : "Unassigned")
                .projectId(issue.getProject().getId())
                .projectName(issue.getProject().getName())
                .tags(issue.getTags().stream()
                        .map(tag -> TagResponse.builder()
                                .id(tag.getId())
                                .name(tag.getName())
                                .color(tag.getColor())
                                .projectId(tag.getProject() != null ? tag.getProject().getId() : null)
                                .build())
                        .toList())
                .attachments(issue.getAttachments().stream()
                        .map(attachment -> AttachmentResponse.builder()
                                .id(attachment.getId())
                                .fileName(attachment.getFileName())
                                .filePath(attachment.getFilePath())
                                .fileSize(attachment.getFileSize())
                                .fileExtension(attachment.getFileExtension())
                                .issueId(attachment.getIssue().getId())
                                .build())
                        .toList())
                .build();
    }

}

