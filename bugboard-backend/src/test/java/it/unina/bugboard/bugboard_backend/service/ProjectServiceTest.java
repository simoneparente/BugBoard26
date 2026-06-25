package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
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
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private UUID projectId;
    private Project dummyProject;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        dummyProject = Project.builder()
                .id(projectId)
                .name("BugBoard Core")
                .description("Descrizione di test")
                .build();
    }

    @Test
    void createProject_Success() {
        // Forza il repository a rispondere che il nome NON esiste
        when(projectRepository.existsByName("BugBoard Core")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(dummyProject);

        Project result = projectService.createProject("BugBoard Core", "Descrizione di test");

        assertNotNull(result);
        assertEquals("BugBoard Core", result.getName());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void createProject_ThrowsException_WhenNameExists() {
        // Forza il repository a rispondere che il nome ESISTE già (testa il ramo d'errore)
        when(projectRepository.existsByName("BugBoard Core")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject("BugBoard Core", "Descrizione di test");
        });

        // Verifica che il salvataggio non venga mai invocato in caso di errore
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void getProjectById_Success() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(dummyProject));

        Project result = projectService.getProjectById(projectId);

        assertNotNull(result);
        assertEquals(projectId, result.getId());
    }

    @Test
    void getProjectById_ThrowsException_WhenNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            projectService.getProjectById(projectId);
        });
    }

    @Test
    void getAllProjects_ReturnsList() {
        List<Project> projectList = List.of(dummyProject);
        when(projectRepository.findAll()).thenReturn(projectList);

        List<Project> result = projectService.getAllProjects();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BugBoard Core", result.get(0).getName());
    }
}