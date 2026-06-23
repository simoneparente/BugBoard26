package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.IssueRequestDTO;
import it.unina.bugboard.bugboard_backend.dto.IssueResponseDTO;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.service.IssueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import it.unina.bugboard.bugboard_backend.entity.Tag;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping
    public ResponseEntity<IssueResponseDTO> createIssue(@RequestBody IssueRequestDTO request) {
        Issue issue = issueService.createIssue(
                request.getTitle(),
                request.getDescription(),
                request.getProjectId(),
                request.getCreatorId(),
                request.getTagIds());
        return new ResponseEntity<>(mapToResponseDTO(issue), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueResponseDTO> getIssueById(@PathVariable UUID id) {
        Issue issue = issueService.getIssueById(id);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }

    // recupera solo le issue di un determinato progetto
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<IssueResponseDTO>> getIssuesByProject(@PathVariable UUID projectId) {
        List<IssueResponseDTO> issues = issueService.getIssuesByProjectId(projectId).stream()
                .map(this::mapToResponseDTO)
                .toList();
        return ResponseEntity.ok(issues);
    }

    // state pattern
    // assegno issue ad utente specifico
    @PutMapping("/{id}/assign")
    public ResponseEntity<IssueResponseDTO> assignIssue(@PathVariable UUID id, @RequestParam UUID assigneeId) {
        Issue issue = issueService.assignIssue(id, assigneeId);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }

    // inizio sviluppo issue
    @PutMapping("/{id}/start-progress")
    public ResponseEntity<IssueResponseDTO> startProgress(@PathVariable UUID id) {
        Issue issue = issueService.startIssueProgress(id);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }

    // accetto risoluzione issue
    @PutMapping("/{id}/accept")
    public ResponseEntity<IssueResponseDTO> acceptIssue(@PathVariable UUID id) {
        Issue issue = issueService.acceptIssue(id);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }

    // ripristino stato precedente
    @PutMapping("/{id}/previous")
    public ResponseEntity<IssueResponseDTO> goToPreviousState(@PathVariable UUID id) {
        Issue issue = issueService.rollbackIssueState(id);
        return ResponseEntity.ok(mapToResponseDTO(issue));
    }

    // rimozione assegnatario
    @DeleteMapping("/{id}/assignee")
    public ResponseEntity<Issue> removeAssignee(@PathVariable UUID id) {
        Issue updatedIssue = issueService.removeIssueAssignee(id);
        return ResponseEntity.ok(updatedIssue);
    }

    private IssueResponseDTO mapToResponseDTO(Issue issue) {
        return IssueResponseDTO.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .projectName(issue.getProject() != null ? issue.getProject().getName() : null)
                // .statusName(issue.getStatus() != null ? issue.getStatus().getName() : null)
                .assigneeUsername(issue.getAssignee() != null ? issue.getAssignee().getUsername() : "Unassigned")
                .tagNames(issue.getTags() != null
                        ? issue.getTags().stream().map(Tag::getName).collect(Collectors.toList())
                        : List.of())
                .attachmentsCount(issue.getAttachments() != null ? issue.getAttachments().size() : 0)
                .build();
    }

}
