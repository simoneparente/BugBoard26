package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.TagRequest;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Tag;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Abilita Mockito per questo test
class TagServiceTest {

    @Mock
    private TagRepository tagRepository; // Creiamo un finto TagRepository

    @Mock
    private ProjectRepository projectRepository; // Creiamo un finto ProjectRepository

    @InjectMocks
    private TagService tagService; // Inseriamo i finti repository dentro il vero TagService

    @Test
    void createTag_Success() {
        // 1. ARRANGE (Prepariamo i dati)
        UUID projectId = UUID.randomUUID();
        TagRequest request = new TagRequest("Bug", "#FF0000", projectId);
        Project mockProject = new Project(projectId, "Progetto Test", null);
        Tag savedTag = new Tag(UUID.randomUUID(), "Bug", "#FF0000", mockProject, null);

        // Istruiamo i mock su come rispondere
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        when(tagRepository.existsByNameAndProjectId(request.getName(), projectId)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);

        // 2. ACT (Eseguiamo il metodo da testare)
        TagResponse response = tagService.createTag(request);

        // 3. ASSERT (Verifichiamo i risultati)
        assertNotNull(response);
        assertEquals("Bug", response.getName());
        assertEquals("#FF0000", response.getColor());
        assertEquals(projectId, response.getProjectId());
        
        // Verifichiamo che il metodo save sia stato chiamato esattamente 1 volta
        verify(tagRepository, times(1)).save(any(Tag.class)); 
    }

    @Test
    void createTag_ThrowsException_WhenProjectNotFound() {
        // ARRANGE
        UUID projectId = UUID.randomUUID();
        TagRequest request = new TagRequest("Bug", "#FF0000", projectId);

        // Diciamo al mock di non trovare il progetto
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        // Ci aspettiamo che lanci una ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.createTag(request);
        });

        // Verifichiamo che non abbia mai provato a salvare il tag
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTag_ThrowsException_WhenTagAlreadyExistsInProject() {
        // ARRANGE
        UUID projectId = UUID.randomUUID();
        TagRequest request = new TagRequest("Bug", "#FF0000", projectId);
        Project mockProject = new Project(projectId, "Progetto Test", null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        // Diciamo al mock che il tag esiste già
        when(tagRepository.existsByNameAndProjectId(request.getName(), projectId)).thenReturn(true);

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(request);
        });
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(tagRepository, never()).save(any(Tag.class));
    }
}