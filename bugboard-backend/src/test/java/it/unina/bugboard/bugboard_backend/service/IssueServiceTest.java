package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.IssueRequest;
import it.unina.bugboard.bugboard_backend.entity.*;
import it.unina.bugboard.bugboard_backend.entity.state.Assigned;
import it.unina.bugboard.bugboard_backend.entity.state.Closed;
import it.unina.bugboard.bugboard_backend.entity.state.InProgress;
import it.unina.bugboard.bugboard_backend.entity.state.MarkedForReview;
import it.unina.bugboard.bugboard_backend.entity.state.ToBeAssigned;
import it.unina.bugboard.bugboard_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private IssueService issueService;

    private UUID projectId;
    private List<UUID> tagIds;
    private Project mockProject;
    private List<Tag> mockTags;
    private Issue mockIssue;
    private User mockUser;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        tagIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        mockProject = Project.builder()
                .id(projectId)
                .name("Test Project")
                .description("bugboard test project")
                .build();

        mockTags = List.of(
                Tag.builder().id(tagIds.get(0)).name("Bug").color("#FF0000").project(mockProject).build(),
                Tag.builder().id(tagIds.get(1)).name("Feature").color("#00FF00").project(mockProject).build());
        mockUser = User.builder().id(UUID.randomUUID()).username("dev_michela").build();
        mockIssue = Issue.builder()
                .id(UUID.randomUUID())
                .title("Sample Issue")
                .description("This is a sample issue for testing.")
                .project(mockProject)
                .status(new ToBeAssigned())
                .priority(IssuePriority.MEDIUM)
                .type(IssueType.BUG)
                .tags(mockTags)
                .createdAt(LocalDateTime.now(ZoneId.systemDefault()))
                .updatedAt(LocalDateTime.now(ZoneId.systemDefault()))
                .build();
    }

    @Test
    void createIssue_Success() {
        String title = "NullPointerException in Login";
        String description = "The server crashes if it receives blank credentials.";

        IssueRequest request = new IssueRequest(title, description, projectId, null, IssueType.BUG,
                IssuePriority.MEDIUM, tagIds);
        LocalDateTime fixedNow = LocalDateTime.now(ZoneId.systemDefault());

        Issue savedIssue = Issue.builder()
                .id(UUID.randomUUID())
                .title(title)
                .description(description)
                .project(mockProject)
                .status(new ToBeAssigned())
                .priority(IssuePriority.MEDIUM)
                .type(IssueType.BUG)
                .tags(mockTags)
                .createdAt(fixedNow)
                .updatedAt(fixedNow)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        when(tagRepository.findAllById(tagIds)).thenReturn(mockTags);
        when(issueRepository.save(any(Issue.class))).thenReturn(savedIssue);

        Issue result = issueService.createIssue(request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals("Test Project", result.getProject().getName());
        assertEquals("TO_BE_ASSIGNED", result.getStatus().getName());
       
        assertEquals(IssuePriority.MEDIUM, result.getPriority());
        assertEquals(IssueType.BUG, result.getType());
        assertEquals(2, result.getTags().size());

        verify(projectRepository, times(1)).findById(projectId);
        verify(tagRepository, times(1)).findAllById(tagIds);
        verify(issueRepository, times(1)).save(any(Issue.class));
    }

    @Test
    void assignIssue_IssueNotFound_ThrowsException() {
        UUID fakeIssueId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        when(issueRepository.findById(fakeIssueId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.assignIssue(fakeIssueId, assigneeId);
        });

        assertTrue(exception.getMessage().contains("Issue not found"));

        verify(userRepository, never()).findById(any());
        verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    void startIssueProgress_Success_FromAssignedToInProgress() {
        UUID issueId = UUID.randomUUID();
        Issue issueInAssignedState = Issue.builder().id(issueId).status(new Assigned()).build();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(issueInAssignedState));

        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.startIssueProgress(issueId);

        assertNotNull(result);
        assertEquals("IN_PROGRESS", result.getStatus().getName());
        assertInstanceOf(InProgress.class, result.getStatus());
        verify(issueRepository, times(1)).save(any(Issue.class));

    }

    @Test
    void startIssueProgress_Failed_WhenStateIsToBeAssigned() {
        UUID issueId = UUID.randomUUID();

        Issue issueWrongState = Issue.builder()
                .id(issueId)
                .status(new ToBeAssigned())
                .build();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(issueWrongState));

        assertThrows(IllegalStateException.class, () -> {
            issueService.startIssueProgress(issueId);
        });

        verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    void removeIssueAssignee_Success_FromAssignedToToBeAssigned() {
        UUID issueId = UUID.randomUUID();
        Issue assignedIssue = Issue.builder()
                .id(issueId)
                .status(new Assigned())
                .assignee(mockUser)
                .build();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(assignedIssue));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.removeIssueAssignee(issueId);

        assertNotNull(result);
        assertNull(result.getAssignee());
        assertEquals("TO_BE_ASSIGNED", result.getStatus().getName());
        assertTrue(result.getStatus() instanceof ToBeAssigned);
    }

    @Test
    void removeIssueAssignee_Failed_WhenStateIsMarkedForReview() {
        UUID issueId = UUID.randomUUID();
        Issue reviewIssue = Issue.builder()
                .id(issueId)
                .status(new MarkedForReview())
                .assignee(mockUser)
                .build();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(reviewIssue));

        assertThrows(IllegalStateException.class, () -> {
            issueService.removeIssueAssignee(issueId);
        });

        verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    void rollbackIssueState_Success_FromInProgressToAssigned() {
        UUID issueId = UUID.randomUUID();
        Issue inProgressIssue = Issue.builder()
                .id(issueId)
                .status(new InProgress())
                .build();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(inProgressIssue));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.rollbackIssueState(issueId);

        assertNotNull(result);
        assertEquals("ASSIGNED", result.getStatus().getName());
        assertInstanceOf(Assigned.class, result.getStatus());
    }

    @Test
    void createIssue_ThrowsException_WhenProjectNotFound() {

        IssueRequest request = new IssueRequest("Titolo", "Descrizione", projectId, null, IssueType.BUG,
                IssuePriority.HIGH, List.of());
        UUID fakeProjectId = UUID.randomUUID();

        when(projectRepository.findById(fakeProjectId)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            issueService.createIssue(request);
        });

        verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    void startIssueProgress_ThrowsException_WhenIssueNotFound() {
        UUID fakeId = UUID.randomUUID();
        when(issueRepository.findById(fakeId)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            issueService.startIssueProgress(fakeId);
        });
    }

    @Test
    void removeIssueAssignee_ThrowsException_WhenIssueNotFound() {
        UUID fakeId = UUID.randomUUID();
        when(issueRepository.findById(fakeId)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            issueService.removeIssueAssignee(fakeId);
        });
    }

    @Test
    void getIssueById_ThrowsException_WhenIssueNotFound() {
        UUID fakeId = UUID.randomUUID();

        when(issueRepository.findById(fakeId)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            issueService.getIssueById(fakeId);
        });
    }

    @Test
    void getIssueById_Found_WhenIssueFound() {
        UUID fakeId = UUID.randomUUID();

        when(issueRepository.findById(fakeId)).thenReturn(Optional.of(mockIssue));

        assertNotNull(issueService.getIssueById(fakeId));
    }

    @Test
    void deleteIssue_ThrowsException_WhenIssueNotFound() {
        UUID fakeId = UUID.randomUUID();

        assertThrows(RuntimeException.class, () -> {
            issueService.deleteIssue(fakeId);
        });
    }

    // --- assignIssue ---

    @Test
    void assignIssue_Success_SetsAssigneeAndSaves() {
        UUID issueId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        Issue issue = Issue.builder().id(issueId).status(new ToBeAssigned()).build();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
        when(userRepository.findById(assigneeId)).thenReturn(Optional.of(mockUser));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.assignIssue(issueId, assigneeId);

        assertNotNull(result);
        assertEquals(mockUser, result.getAssignee());
        verify(issueRepository).save(issue);
    }

    @Test
    void assignIssue_ThrowsException_WhenAssigneeNotFound() {
        UUID issueId = UUID.randomUUID();
        UUID fakeAssigneeId = UUID.randomUUID();

        Issue issue = Issue.builder().id(issueId).status(new ToBeAssigned()).build();
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
        when(userRepository.findById(fakeAssigneeId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> issueService.assignIssue(issueId, fakeAssigneeId));

        assertTrue(ex.getMessage().contains("Assignee not found with ID: " + fakeAssigneeId));
        verify(issueRepository, never()).save(any());
    }

    // --- acceptIssue ---

    @Test
    void acceptIssue_Success_FromMarkedForReviewToClosed() {
        UUID issueId = UUID.randomUUID();
        Issue issue = Issue.builder().id(issueId).status(new MarkedForReview()).build();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.acceptIssue(issueId);

        assertNotNull(result);
        assertEquals("CLOSED", result.getStatus().getName());
        assertInstanceOf(Closed.class, result.getStatus());
        verify(issueRepository).save(issue);
    }

    @Test
    void acceptIssue_ThrowsException_WhenIssueNotFound() {
        UUID fakeId = UUID.randomUUID();
        when(issueRepository.findById(fakeId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> issueService.acceptIssue(fakeId));

        assertTrue(ex.getMessage().contains("Issue not found with ID: " + fakeId));
        verify(issueRepository, never()).save(any());
    }

    @Test
    void acceptIssue_ThrowsIllegalStateException_WhenStateIsNotMarkedForReview() {
        UUID issueId = UUID.randomUUID();
        Issue issue = Issue.builder().id(issueId).status(new InProgress()).build();
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));

        assertThrows(IllegalStateException.class, () -> issueService.acceptIssue(issueId));
        verify(issueRepository, never()).save(any());
    }

    // --- getAllIssues ---

    @Test
    void getAllIssues_ReturnsAllIssues() {
        Issue issue2 = Issue.builder().id(UUID.randomUUID()).title("Second Issue").build();
        when(issueRepository.findAll()).thenReturn(List.of(mockIssue, issue2));

        List<Issue> result = issueService.getAllIssues();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(issueRepository).findAll();
    }

    @Test
    void getAllIssues_ReturnsEmptyList_WhenNoIssuesExist() {
        when(issueRepository.findAll()).thenReturn(List.of());

        List<Issue> result = issueService.getAllIssues();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(issueRepository).findAll();
    }

}
