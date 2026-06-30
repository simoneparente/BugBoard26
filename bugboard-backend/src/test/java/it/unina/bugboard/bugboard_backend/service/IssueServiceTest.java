package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.entity.*;
import it.unina.bugboard.bugboard_backend.entity.state.Assigned;
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
    }

    @Test
    void createIssue_Success() {
        // Arrange
        String title = "NullPointerException in Login";
        String description = "The server crashes if it receives blank credentials.";

        Issue savedIssue = Issue.builder()
                .id(UUID.randomUUID())
                .title(title)
                .description(description)
                .project(mockProject)
                .status(new ToBeAssigned())
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
        assertEquals("TO_BE_ASSIGNED", result.getStatus().getName());
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

    @Test
    void startIssueProgress_Success_FromAssignedToInProgress() {
        UUID issueId = UUID.randomUUID();
        // l'issue deve partire da assigned per andare in progress
        Issue issueInAssignedState = Issue.builder().id(issueId).status(new Assigned()).build();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(issueInAssignedState));

        // mock del salvataggio restituendo l'issue modificata in memoria
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.startIssueProgress(issueId);

        assertNotNull(result);
        assertEquals("IN_PROGRESS", result.getStatus().getName());
        assertTrue(result.getStatus() instanceof InProgress);
        verify(issueRepository, times(1)).save(any(Issue.class));

    }

    @Test
    void startIssueProgress_Failed_WhenStateIsToBeAssigned() {
        UUID issueId = UUID.randomUUID();
        // Se è ancora TO_BE_ASSIGNED, non si può iniziare il progresso senza
        // assegnatario!
        Issue issueWrongState = Issue.builder()
                .id(issueId)
                .status(new ToBeAssigned())
                .build();

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(issueWrongState));

        // Verifichiamo che lanci l'eccezione definita in BaseStatus
        assertThrows(IllegalStateException.class, () -> {
            issueService.startIssueProgress(issueId);
        });

        verify(issueRepository, never()).save(any(Issue.class));
    }

    @Test
    // rimozione assegnatario
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
        assertNull(result.getAssignee()); // L'utente deve essere ripulito
        assertEquals("TO_BE_ASSIGNED", result.getStatus().getName());
        assertTrue(result.getStatus() instanceof ToBeAssigned);
    }

    @Test
    void removeIssueAssignee_Failed_WhenStateIsMarkedForReview() {
        UUID issueId = UUID.randomUUID();
        // Se la issue è già in Review, non puoi rimuovere l'assegnatario così
        // liberamente
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

    //previous state
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
        assertTrue(result.getStatus() instanceof Assigned);
    }

    @Test
    void createIssue_ThrowsException_WhenProjectNotFound() {
        // 1. Definiamo i parametri mancanti richiesti dalla firma reale
        UUID fakeProjectId = UUID.randomUUID();
        UUID fakeCreatorId = UUID.randomUUID();
        java.util.List<UUID> fakeTagIds = java.util.List.of(); // Lista vuota di tag

        // 2. Forza il project repository a restituire un Optional vuoto per simulare l'errore
        when(projectRepository.findById(fakeProjectId)).thenReturn(java.util.Optional.empty());

        // 3. Eseguiamo l'asserzione passando tutti e 5 i parametri corretti
        assertThrows(RuntimeException.class, () -> {
            issueService.createIssue("Titolo", "Descrizione", fakeProjectId, fakeCreatorId, fakeTagIds);
        });

        // 4. Verifica che il salvataggio non venga mai invocato
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

    
}

