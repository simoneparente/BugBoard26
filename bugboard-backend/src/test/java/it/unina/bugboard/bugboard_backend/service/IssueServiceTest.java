package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.entity.*;
import it.unina.bugboard.bugboard_backend.entity.state.OpenStatus;
import it.unina.bugboard.bugboard_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        tagIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        mockProject = Project.builder()
                .id(projectId)
                .name("Test Project")
                .description("progetto di test per bugboard")
                .build();

        mockTags = List.of(
                Tag.builder().id(tagIds.get(0)).name("Bug").color("#FF0000").project(mockProject).build(),
                Tag.builder().id(tagIds.get(1)).name("Feature").color("#00FF00").project(mockProject).build());
    }

    @Test
    void createIssue_Success() {
        // Arrange
        String title = "NullPointerException in Login";
        String description = "Il server si blocca se riceve credenziali vuote.";

        Issue savedIssue = Issue.builder()
                .id(UUID.randomUUID())
                .title(title)
                .description(description)
                .project(mockProject)
                .status(new OpenStatus()) // Istanza iniziale dello State Pattern
                .priority(IssuePriority.MEDIUM)
                .type(IssueType.BUG)
                .tags(mockTags)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        when(tagRepository.findAllById(tagIds)).thenReturn(mockTags);
        when(issueRepository.save(any(Issue.class))).thenReturn(savedIssue);

        // Act
        Issue result = issueService.createIssue(title, description, projectId, UUID.randomUUID(), tagIds);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals("Test Project", result.getProject().getName());

        /*
         * assertNotNull(result.getStatus());
         * assertEquals("OPEN", result.getStatus().getStatusName());
         * assertTrue(result.getStatus() instanceof OpenStatus);
         */

        assertEquals(IssuePriority.MEDIUM, result.getPriority());
        assertEquals(IssueType.BUG, result.getType());
        assertEquals(2, result.getTags().size());

        verify(issueRepository, times(1)).save(any(Issue.class));
    }

    @Test
    void assignIssue_IssueNotFound_ThrowsException() {
        // 1. ARRANGE
        UUID fakeIssueId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        // Simuliamo che la issue cercata non esista
        when(issueRepository.findById(fakeIssueId)).thenReturn(Optional.empty());

        // 2. ACT & 3. ASSERT
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            issueService.assignIssue(fakeIssueId, assigneeId);
        });

        assertTrue(exception.getMessage().contains("Issue non trovata"));
        
        // Verifichiamo che il processo si sia interrotto subito
        verify(userRepository, never()).findById(any());
        verify(issueRepository, never()).save(any(Issue.class));
    }
}
