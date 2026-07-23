package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.AttachmentMetadataRequest;
import it.unina.bugboard.bugboard_backend.dto.IssueRequest;
import it.unina.bugboard.bugboard_backend.entity.*;
import it.unina.bugboard.bugboard_backend.repository.*;
import it.unina.bugboard.bugboard_backend.exception.OperationNotAllowedException;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;

import java.util.Objects;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class IssueService {

    private static final String ISSUE_NOT_FOUND_MSG = "Issue not found with ID: ";

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional
    public Issue createIssue(UUID projectId, IssueRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found."));

        List<TagResponse> tagResponses = request.getTags();
        List<Tag> tags = tagResponses == null || tagResponses.isEmpty()
                ? List.of()
                : tagResponses.stream()
                        .filter(Objects::nonNull)
                        .map(tagResponse -> Tag.builder()
                                .id(tagResponse.getId())
                                .name(tagResponse.getName())
                                .color(tagResponse.getColor())
                                .project(project)
                                .build())
                        .toList();

        User assignee = null;
        if (request.getAssigneeUsername() != null) {
            assignee = userRepository.findByUsername(request.getAssigneeUsername())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Assignee not found with username: " + request.getAssigneeUsername()));
        }

        Issue issue = Issue.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .priority(request.getPriority())
                .type(request.getType())
                .status(request.getStatus() != null ? request.getStatus() : IssueStatus.TO_DO)
                .tags(tags)
                .assignee(assignee)
                .attachments(List.of()) // Issue is created without attachments; they will be added after the issue is
                                        // saved
                .build();

        Issue savedIssue = issueRepository.save(issue);

        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (AttachmentMetadataRequest meta : request.getAttachments()) {
                Attachment attachment = Attachment.builder()
                        .fileName(meta.getOriginalFileName())
                        .filePath(meta.getBlobFileName())
                        .fileSize(meta.getFileSize())
                        .fileExtension(meta.getExtension())
                        .issue(savedIssue)
                        .build();
                attachmentRepository.save(attachment);
            }
        }

        return savedIssue;
    }

    @Transactional
    public Issue assignIssueToUser(UUID issueId, UUID userId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));
        if (issue.getStatus() == IssueStatus.CLOSED) {
            throw new OperationNotAllowedException("Cannot assign a user to a CLOSED issue.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        issue.setAssignee(user);
        issueRepository.save(issue);
        return issue;
    }

    @Transactional
    public Issue setStatus(UUID issueId, IssueStatus newStatus) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));

        if (!isIssueAssigned(issue) && newStatus == IssueStatus.IN_PROGRESS) {
            throw new OperationNotAllowedException("Cannot set status to IN_PROGRESS for an unassigned issue.");
        }
        if (issue.getStatus() == IssueStatus.COMPLETED && newStatus == IssueStatus.CLOSED) {
            throw new OperationNotAllowedException("Cannot close a COMPLETED issue.");
        }
        if (newStatus == IssueStatus.TO_DO || newStatus == IssueStatus.CLOSED) {
            issue.setAssignee(null);
        }
        issue.setStatus(newStatus);
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue startIssueProgress(UUID issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));
        if (!isIssueAssigned(issue)) {
            throw new OperationNotAllowedException("Cannot set status to IN_PROGRESS for an unassigned issue.");
        }
        issue.setStatus(IssueStatus.IN_PROGRESS);
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue acceptIssue(UUID issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));
        issue.setStatus(IssueStatus.MARKED_FOR_REVIEW);
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue rollbackIssueState(UUID issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));
        IssueStatus previous = switch (issue.getStatus()) {
            case IN_PROGRESS -> IssueStatus.TO_DO;
            case MARKED_FOR_REVIEW -> IssueStatus.IN_PROGRESS;
            case NOT_FIXED -> IssueStatus.IN_PROGRESS;
            case COMPLETED -> IssueStatus.MARKED_FOR_REVIEW;
            case CLOSED -> IssueStatus.TO_DO;
            default -> throw new OperationNotAllowedException("Cannot rollback from status: " + issue.getStatus());
        };
        if (previous == IssueStatus.TO_DO) {
            issue.setAssignee(null);
        }
        issue.setStatus(previous);
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue removeIssueAssignee(UUID issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException(ISSUE_NOT_FOUND_MSG + issueId));

        if (issue.getStatus() == IssueStatus.COMPLETED) {
            throw new OperationNotAllowedException("Cannot remove assignee from a COMPLETED issue.");
        }

        issue.setAssignee(null);
        issue.setStatus(IssueStatus.TO_DO);
        return issueRepository.save(issue);
    }

    @Transactional(readOnly = true)
    public Issue getIssueByIdAndProjectId(UUID issueId, UUID projectId) {
        Issue issue = issueRepository.findByIdAndProjectId(issueId, projectId);
        if (issue == null) {
            throw new ResourceNotFoundException(ISSUE_NOT_FOUND_MSG + issueId + " in project " + projectId);
        }
        return issue;
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
    public Page<Issue> getIssuesByProjectId(UUID projectId, String status, String priority, Pageable pageable) {
        return getIssuesByProjectId(projectId, status, priority, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Issue> getIssuesByProjectId(UUID projectId, String status, String priority, String search, Pageable pageable) {
        System.out.println("⚙️ [3. Service] Inizio elaborazione filtri...");
        IssueStatus statusEnum = parseEnum(IssueStatus.class, status);
        IssuePriority priorityEnum = parseEnum(IssuePriority.class, priority);
        String searchPattern = (search != null && !search.isBlank()) ? "%" + search.trim().toLowerCase() + "%" : null;

        return issueRepository.findByProjectIdAndFilters(projectId, statusEnum, priorityEnum, searchPattern, pageable);
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

    private boolean isIssueAssigned(Issue issue) {
        return issue.getAssignee() != null;
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumType, String value) {
    if (value == null || value.isBlank() || value.equalsIgnoreCase("ALL")) {
        return null;
    }
    try {
        return Enum.valueOf(enumType, value.toUpperCase());
    } catch (IllegalArgumentException e) {
        return null;
    }
}

}
