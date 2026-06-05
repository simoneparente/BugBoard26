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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enable Mockito for this test
class TagServiceTest {

    @Mock
    private TagRepository tagRepository; // Create a mock TagRepository

    @Mock
    private ProjectRepository projectRepository; // Create a mock ProjectRepository

    @InjectMocks
    private TagService tagService; // Inject the mock repositories into the real TagService

    @Test
    void createTag_Success() {
        // 1. ARRANGE (Prepare the data)
        UUID projectId = UUID.randomUUID();
        TagRequest request = new TagRequest("Bug", "#FF0000", projectId);
        Project mockProject = new Project(projectId, "Test Project", null);
        Tag savedTag = new Tag(UUID.randomUUID(), "Bug", "#FF0000", mockProject, null);

        // Instruct the mocks on how to respond
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        when(tagRepository.existsByNameAndProjectId(request.getName(), projectId)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);

        // 2. ACT (Execute the method being tested)
        TagResponse response = tagService.createTag(request);

        // 3. ASSERT (Verify the results)
        assertNotNull(response);
        assertEquals("Bug", response.getName());
        assertEquals("#FF0000", response.getColor());
        assertEquals(projectId, response.getProjectId());

        // Verify that the save method was called exactly 1 time
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void createTag_ThrowsException_WhenProjectNotFound() {
        // ARRANGE
        UUID projectId = UUID.randomUUID();
        TagRequest request = new TagRequest("Bug", "#FF0000", projectId);

        // Tell the mock to not find the project
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        // We expect it to throw a ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.createTag(request);
        });

        // Verify that it never tried to save the tag
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTag_ThrowsException_WhenTagAlreadyExistsInProject() {
        // ARRANGE
        UUID projectId = UUID.randomUUID();
        TagRequest request = new TagRequest("Bug", "#FF0000", projectId);
        Project mockProject = new Project(projectId, "Test Project", null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
        // Tell the mock that the tag already exists
        when(tagRepository.existsByNameAndProjectId(request.getName(), projectId)).thenReturn(true);

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag(request);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void getAllTagsByProjectId_Success() {
        // ARRANGE
        UUID projectId = UUID.randomUUID();
        Project mockProject = new Project(projectId, "Test Project", null);

        // Create a mock list with two tags
        Tag tag1 = new Tag(UUID.randomUUID(), "Bug", "#FF0000", mockProject, null);
        Tag tag2 = new Tag(UUID.randomUUID(), "Feature", "#00FF00", mockProject, null);

        when(tagRepository.findByProjectId(projectId)).thenReturn(List.of(tag1, tag2));

        // ACT
        List<TagResponse> responses = tagService.getAllTagsByProjectId(projectId);

        // ASSERT
        assertNotNull(responses);
        assertEquals(2, responses.size()); // Verify that it returned exactly two tags
        assertEquals("Bug", responses.get(0).getName());
        assertEquals("Feature", responses.get(1).getName());

        verify(tagRepository, times(1)).findByProjectId(projectId);
    }

    @Test
    void getTagById_Success() {
        // ARRANGE
        UUID tagId = UUID.randomUUID();
        Project mockProject = new Project(UUID.randomUUID(), "Test Project", null);
        Tag mockTag = new Tag(tagId, "Bug", "#FF0000", mockProject, null);

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(mockTag));

        // ACT
        TagResponse response = tagService.getTagById(tagId);

        // ASSERT
        assertNotNull(response);
        assertEquals(tagId, response.getId());
        assertEquals("Bug", response.getName());

        verify(tagRepository, times(1)).findById(tagId);
    }

    @Test
    void getTagById_ThrowsException_WhenTagNotFound() {
        // ARRANGE
        UUID tagId = UUID.randomUUID();

        // Tell the mock database to not find anything
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.getTagById(tagId);
        });

        verify(tagRepository, times(1)).findById(tagId);
    }
}