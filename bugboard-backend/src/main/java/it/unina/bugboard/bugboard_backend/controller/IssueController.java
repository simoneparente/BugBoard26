package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.AttachmentResponse;
import it.unina.bugboard.bugboard_backend.dto.IssueRequest;
import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.service.IssueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import it.unina.bugboard.bugboard_backend.entity.IssueStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectKey}/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    public ResponseEntity<IssueResponse> createIssue(@PathVariable String projectKey,
            @Valid @RequestBody IssueRequest request) {
        Issue issue = issueService.createIssue(projectKey, request);
        return new ResponseEntity<>(mapToResponseDTO(issue), HttpStatus.CREATED);
    }

    @GetMapping("/{sequenceNumber}")
    public ResponseEntity<IssueResponse> getIssueByProjectKeyAndSequenceNumber(@PathVariable String projectKey, @PathVariable Long sequenceNumber) {
        Issue issue = issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }

    @PutMapping("/{sequenceNumber}")
    public ResponseEntity<IssueResponse> updateIssue(@PathVariable String projectKey,
            @PathVariable Long sequenceNumber,
            @Valid @RequestBody IssueRequest request) {
        Issue updatedIssue = issueService.updateIssue(projectKey, sequenceNumber, request);
        return ResponseEntity.ok(mapToResponseDTO(updatedIssue));
    }

    @GetMapping
    public ResponseEntity<Page<IssueResponse>> getIssuesByProject(@PathVariable String projectKey,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "ALL") String priority,
            @RequestParam(required = false) String search, Pageable pageable) {

        Page<IssueResponse> issues = issueService.getIssuesByProjectKey(projectKey, status, priority, search, pageable)
                .map(this::mapToResponseDTO);
        
        return ResponseEntity.ok(issues);
    }

    @PutMapping("/{sequenceNumber}/assign")
    public ResponseEntity<IssueResponse> assignIssue(@PathVariable String projectKey, @PathVariable Long sequenceNumber, @RequestParam UUID assigneeId) {
        Issue issue = issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
        Issue updatedIssue = issueService.assignIssueToUser(issue.getId(), assigneeId);
        return ResponseEntity.ok(mapToResponseDTO(updatedIssue));
    }

    @PutMapping("/{sequenceNumber}/status")
    public ResponseEntity<IssueResponse> setStatus(@PathVariable String projectKey, @PathVariable Long sequenceNumber, @RequestParam IssueStatus status) {
        Issue issue = issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
        Issue updatedIssue = issueService.setStatus(issue.getId(), status);
        return ResponseEntity.ok(mapToResponseDTO(updatedIssue));
    }

    @PutMapping("/{sequenceNumber}/start-progress")
    public ResponseEntity<IssueResponse> startProgress(@PathVariable String projectKey, @PathVariable Long sequenceNumber) {
        Issue issue = issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
        Issue updatedIssue = issueService.startIssueProgress(issue.getId());
        return ResponseEntity.ok(mapToResponseDTO(updatedIssue));
    }

    @PutMapping("/{sequenceNumber}/accept")
    public ResponseEntity<IssueResponse> acceptIssue(@PathVariable String projectKey, @PathVariable Long sequenceNumber) {
        Issue issue = issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
        Issue updatedIssue = issueService.acceptIssue(issue.getId());
        return ResponseEntity.ok(mapToResponseDTO(updatedIssue));
    }

    @PutMapping("/{sequenceNumber}/previous")
    public ResponseEntity<IssueResponse> goToPreviousState(@PathVariable String projectKey, @PathVariable Long sequenceNumber) {
        Issue issue = issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
        Issue updatedIssue = issueService.rollbackIssueState(issue.getId());
        return ResponseEntity.ok(mapToResponseDTO(updatedIssue));
    }

    @DeleteMapping("/{sequenceNumber}/assignee")
    public ResponseEntity<IssueResponse> removeAssignee(@PathVariable String projectKey, @PathVariable Long sequenceNumber) {
        Issue issue = issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
        Issue updatedIssue = issueService.removeIssueAssignee(issue.getId());
        return ResponseEntity.ok(mapToResponseDTO(updatedIssue));
    }

    @DeleteMapping("/{sequenceNumber}")
    public ResponseEntity<Void> deleteIssue(@PathVariable String projectKey, @PathVariable Long sequenceNumber) {
        Issue issue = issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
        issueService.deleteIssue(issue.getId());
        return ResponseEntity.noContent().build();
    }

    private IssueResponse mapToResponseDTO(Issue issue) {
        return IssueResponse.builder()
                .id(issue.getId())
                .sequenceNumber(issue.getSequenceNumber())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .status(issue.getStatus().name())
                .priority(issue.getPriority().name())
                .type(issue.getType().name())
                .assigneeUsername(issue.getAssignee() != null ? issue.getAssignee().getUsername() : "Unassigned")
                .projectId(issue.getProject().getId())
                .projectKey(issue.getProject().getKey())
                .projectName(issue.getProject().getName())
                .tags(issue.getTags().stream()
                        .map(tag -> TagResponse.builder()
                                .id(tag.getId())
                                .name(tag.getName())
                                .color(tag.getColor())
                                .projectKey(tag.getProject() != null ? tag.getProject().getKey() : null)
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
