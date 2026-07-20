package it.unina.bugboard.bugboard_backend.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.dto.ProjectResponse;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.dto.UserResponse;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Tag;
import it.unina.bugboard.bugboard_backend.entity.User;

@ExtendWith(MockitoExtension.class)
class ProjectMapperTest {

    @Mock
    private IssueMapper issueMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ProjectMapper projectMapper;

    private UUID projectId;
    private Project dummyProject;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        now = LocalDateTime.now();

        dummyProject = Project.builder()
                .id(projectId)
                .name("Test Project")
                .description("Test description")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void toResponse_Success_WithAllData() {
        // Arrange
        UUID issueId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Issue issue = Issue.builder()
                .id(issueId)
                .title("Test Issue")
                .build();

        Tag tag = Tag.builder()
                .id(tagId)
                .name("Bug")
                .build();

        User user = User.builder()
                .id(userId)
                .username("testuser")
                .build();

        dummyProject.setIssues(List.of(issue));
        dummyProject.setTags(List.of(tag));
        dummyProject.setMembers(List.of(user));

        // Mock the mappers
        when(issueMapper.toResponse(issue)).thenReturn(
                IssueResponse.builder().id(issueId).title("Test Issue").build()
        );
        when(tagMapper.toResponse(tag)).thenReturn(
                TagResponse.builder().id(tagId).name("Bug").build()
        );
        when(userMapper.toResponse(user)).thenReturn(
                UserResponse.builder().id(userId).username("testuser").build()
        );

        // Act
        ProjectResponse response = projectMapper.toResponse(dummyProject);

        // Assert
        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertEquals("Test Project", response.getName());
        assertEquals("Test description", response.getDescription());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getUpdatedAt());
        assertEquals(1, response.getIssues().size());
        assertEquals(1, response.getTags().size());
        assertEquals(1, response.getMembers().size());

        verify(issueMapper, times(1)).toResponse(issue);
        verify(tagMapper, times(1)).toResponse(tag);
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    void toResponse_Success_WithEmptyCollections() {
        // Arrange
        dummyProject.setIssues(null);
        dummyProject.setTags(null);
        dummyProject.setMembers(null);

        // Act
        ProjectResponse response = projectMapper.toResponse(dummyProject);

        // Assert
        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertEquals("Test Project", response.getName());
        assertTrue(response.getIssues().isEmpty());
        assertTrue(response.getTags().isEmpty());
        assertTrue(response.getMembers().isEmpty());
    }

    @Test
    void toResponse_Success_WithEmptyLists() {
        // Arrange
        dummyProject.setIssues(List.of());
        dummyProject.setTags(List.of());
        dummyProject.setMembers(List.of());

        // Act
        ProjectResponse response = projectMapper.toResponse(dummyProject);

        // Assert
        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertTrue(response.getIssues().isEmpty());
        assertTrue(response.getTags().isEmpty());
        assertTrue(response.getMembers().isEmpty());

        // Verify mappers were not called
        verify(issueMapper, never()).toResponse(any());
        verify(tagMapper, never()).toResponse(any());
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void toResponse_ReturnsNull_WhenProjectIsNull() {
        // Act
        ProjectResponse response = projectMapper.toResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void toResponse_WithMultipleIssuesTagsAndMembers() {
        // Arrange
        Issue issue1 = Issue.builder().id(UUID.randomUUID()).title("Issue 1").build();
        Issue issue2 = Issue.builder().id(UUID.randomUUID()).title("Issue 2").build();
        Tag tag1 = Tag.builder().id(UUID.randomUUID()).name("Bug").build();
        Tag tag2 = Tag.builder().id(UUID.randomUUID()).name("Feature").build();
        User user1 = User.builder().id(UUID.randomUUID()).username("user1").build();
        User user2 = User.builder().id(UUID.randomUUID()).username("user2").build();

        dummyProject.setIssues(List.of(issue1, issue2));
        dummyProject.setTags(List.of(tag1, tag2));
        dummyProject.setMembers(List.of(user1, user2));

        when(issueMapper.toResponse(any())).thenReturn(IssueResponse.builder().build());
        when(tagMapper.toResponse(any())).thenReturn(TagResponse.builder().build());
        when(userMapper.toResponse(any())).thenReturn(UserResponse.builder().build());

        // Act
        ProjectResponse response = projectMapper.toResponse(dummyProject);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getIssues().size());
        assertEquals(2, response.getTags().size());
        assertEquals(2, response.getMembers().size());

        verify(issueMapper, times(2)).toResponse(any());
        verify(tagMapper, times(2)).toResponse(any());
        verify(userMapper, times(2)).toResponse(any());
    }
}
