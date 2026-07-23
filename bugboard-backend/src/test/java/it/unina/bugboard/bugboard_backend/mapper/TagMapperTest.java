package it.unina.bugboard.bugboard_backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Tag;
import it.unina.bugboard.bugboard_backend.service.ProjectService;

@ExtendWith(MockitoExtension.class)
class TagMapperTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private TagMapper tagMapper;

    private UUID tagId;
    private UUID projectId;
    private Project dummyProject;

    @BeforeEach
    void setUp() {
        tagId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        dummyProject = Project.builder()
                .id(projectId)
                .key("FRONT")
                .name("Test Project")
                .description("Test description")
                .build();
    }

    @Test
    void toResponse_Success() {
        // Arrange
        Tag tag = Tag.builder()
                .id(tagId)
                .name("Bug")
                .color("#FF0000")
                .project(dummyProject)
                .build();

        // Act
        TagResponse response = tagMapper.toResponse(tag);

        // Assert
        assertNotNull(response);
        assertEquals(tagId, response.getId());
        assertEquals("Bug", response.getName());
        assertEquals("#FF0000", response.getColor());
        assertEquals("FRONT", response.getProjectKey());
    }

    @Test
    void toResponse_ReturnsNull_WhenTagIsNull() {
        // Act
        TagResponse response = tagMapper.toResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void toResponse_WithNullProject() {
        // Arrange
        Tag tag = Tag.builder()
                .id(tagId)
                .name("Orphan Tag")
                .color("#00FF00")
                .project(null)
                .build();

        // Act
        TagResponse response = tagMapper.toResponse(tag);

        // Assert
        assertNotNull(response);
        assertEquals("Orphan Tag", response.getName());
        assertNull(response.getProjectKey());
    }

    @Test
    void mapToEntity_Success() {
        // Arrange
        TagResponse tagResponse = TagResponse.builder()
                .id(tagId)
                .name("Feature")
                .color("#0000FF")
                .projectKey("FRONT")
                .build();

        when(projectService.getProjectByKey("FRONT")).thenReturn(dummyProject);

        // Act
        Tag tag = tagMapper.mapToEntity(tagResponse);

        // Assert
        assertNotNull(tag);
        assertEquals(tagId, tag.getId());
        assertEquals("Feature", tag.getName());
        assertEquals("#0000FF", tag.getColor());
        assertEquals("FRONT", tag.getProject().getKey());
        verify(projectService, times(1)).getProjectByKey("FRONT");
    }

    @Test
    void mapToEntity_ThrowsException_WhenProjectNotFound() {
        // Arrange
        TagResponse tagResponse = TagResponse.builder()
                .id(tagId)
                .name("Nonexistent Project Tag")
                .color("#FFFFFF")
                .projectKey("INVALID")
                .build();

        when(projectService.getProjectByKey(any(String.class))).thenThrow(new RuntimeException("Project not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            tagMapper.mapToEntity(tagResponse);
        });

        verify(projectService, times(1)).getProjectByKey(any(String.class));
    }
}
