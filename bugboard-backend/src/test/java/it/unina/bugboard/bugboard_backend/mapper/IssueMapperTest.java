package it.unina.bugboard.bugboard_backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.IssuePriority;
import it.unina.bugboard.bugboard_backend.entity.IssueStatus;
import it.unina.bugboard.bugboard_backend.entity.IssueType;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.User;

class IssueMapperTest {

    private IssueMapper issueMapper;

    @BeforeEach
    void setUp() {
        issueMapper = new IssueMapper();
    }

    @Test
    void toResponse_Success_WithAssignee() {
        // Arrange
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        User assignee = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .build();

        Issue issue = Issue.builder()
                .id(issueId)
                .title("Test Issue")
                .description("Test description")
                .status(IssueStatus.IN_PROGRESS)
                .priority(IssuePriority.HIGH)
                .type(IssueType.BUG)
                .createdAt(now)
                .updatedAt(now)
                .assignee(assignee)
                .project(project)
                .build();

        // Act
        IssueResponse response = issueMapper.toResponse(issue);

        // Assert
        assertNotNull(response);
        assertEquals(issueId, response.getId());
        assertEquals("Test Issue", response.getTitle());
        assertEquals("Test description", response.getDescription());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals("HIGH", response.getPriority());
        assertEquals("BUG", response.getType());
        assertEquals("testuser", response.getAssigneeUsername());
        assertEquals(projectId, response.getProjectId());
        assertEquals("Test Project", response.getProjectName());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
    }

    @Test
    void toResponse_Success_WithoutAssignee() {
        // Arrange
        UUID issueId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        Project project = Project.builder()
                .id(projectId)
                .name("Test Project")
                .build();

        Issue issue = Issue.builder()
                .id(issueId)
                .title("Unassigned Issue")
                .description("No assignee")
                .status(IssueStatus.CLOSED)
                .priority(IssuePriority.LOW)
                .type(IssueType.FEATURE)
                .assignee(null)
                .project(project)
                .build();

        // Act
        IssueResponse response = issueMapper.toResponse(issue);

        // Assert
        assertNotNull(response);
        assertEquals("Unassigned", response.getAssigneeUsername());
    }

    @Test
    void toResponse_ReturnsNull_WhenIssueIsNull() {
        // Act
        IssueResponse response = issueMapper.toResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void toResponse_WithDifferentPriorities() {
        // Test MEDIUM priority
        Project project = Project.builder()
                .id(UUID.randomUUID())
                .name("Project")
                .build();

        Issue issue = Issue.builder()
                .id(UUID.randomUUID())
                .title("Medium Priority Issue")
                .description("Test")
                .status(IssueStatus.IN_PROGRESS)
                .priority(IssuePriority.MEDIUM)
                .type(IssueType.BUG)
                .project(project)
                .build();

        IssueResponse response = issueMapper.toResponse(issue);

        assertNotNull(response);
        assertEquals("MEDIUM", response.getPriority());
        assertEquals("BUG", response.getType());
    }
}
