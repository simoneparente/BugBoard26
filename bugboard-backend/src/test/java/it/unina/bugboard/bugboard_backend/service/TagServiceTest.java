package it.unina.bugboard.bugboard_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import it.unina.bugboard.bugboard_backend.dto.TagRequest;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.Tag;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.exception.TagDuplicateException;
import it.unina.bugboard.bugboard_backend.exception.UnauthorizedException;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.repository.TagRepository;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void createTag_Success() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TagRequest request = new TagRequest("Bug", "#FF0000", projectId);

        Project mockProject = Project.builder()
            .id(projectId)
            .name("Test Project")
            .build();

        User mockUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .role(Role.ADMIN)
            .build();

        Tag savedTag = new Tag(UUID.randomUUID(), "Bug", "#FF0000", mockProject, null);

        // Mock SecurityContextHolder and Authentication
        try (MockedStatic<SecurityContextHolder> mockedSecurityHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            when(authentication.getName()).thenReturn(userId.toString());
            when(authentication.isAuthenticated()).thenReturn(true);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
            when(tagRepository.existsByNameAndProjectId(request.getName(), projectId)).thenReturn(false);
            when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);

            // ACT
            TagResponse response = tagService.createTag(request);

            // ASSERT
            assertNotNull(response);
            assertEquals("Bug", response.getName());
            assertEquals("#FF0000", response.getColor());
            assertEquals(projectId, response.getProjectId());

            verify(tagRepository, times(1)).save(any(Tag.class));
        }
    }

    @Test
    void createTag_ThrowsException_WhenProjectNotFound() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TagRequest request = new TagRequest("Bug", "#FF0000", projectId);

        User mockUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .role(Role.ADMIN)
            .build();

        try (MockedStatic<SecurityContextHolder> mockedSecurityHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            when(authentication.getName()).thenReturn(userId.toString());
            when(authentication.isAuthenticated()).thenReturn(true);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThrows(ResourceNotFoundException.class, () -> {
                tagService.createTag(request);
            });

            verify(tagRepository, never()).save(any(Tag.class));
        }
    }

    @Test
    void createTag_ThrowsException_WhenTagAlreadyExistsInProject() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TagRequest request = new TagRequest("Bug", "#FF0000", projectId);

        Project mockProject = Project.builder()
            .id(projectId)
            .name("Test Project")
            .build();

        User mockUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .role(Role.ADMIN)
            .build();

        try (MockedStatic<SecurityContextHolder> mockedSecurityHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            when(authentication.getName()).thenReturn(userId.toString());
            when(authentication.isAuthenticated()).thenReturn(true);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));
            when(tagRepository.existsByNameAndProjectId(request.getName(), projectId)).thenReturn(true);

            // ACT & ASSERT
            TagDuplicateException exception = assertThrows(TagDuplicateException.class, () -> {
                tagService.createTag(request);
            });

            assertTrue(exception.getMessage().contains("already exists"));
            verify(tagRepository, never()).save(any(Tag.class));
        }
    }

    @Test
    void getAllTagsByProjectId_Success() {
        // ARRANGE
        UUID projectId = UUID.randomUUID();
        Project mockProject = Project.builder()
            .id(projectId)
            .name("Test Project")
            .build();

        Tag tag1 = new Tag(UUID.randomUUID(), "Bug", "#FF0000", mockProject, null);
        Tag tag2 = new Tag(UUID.randomUUID(), "Feature", "#00FF00", mockProject, null);

        when(tagRepository.findByProjectId(projectId)).thenReturn(List.of(tag1, tag2));

        // ACT
        List<TagResponse> responses = tagService.getAllTagsByProjectId(projectId);

        // ASSERT
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Bug", responses.get(0).getName());
        assertEquals("Feature", responses.get(1).getName());

        verify(tagRepository, times(1)).findByProjectId(projectId);
    }

    @Test
    void getTagById_Success() {
        // ARRANGE
        UUID tagId = UUID.randomUUID();
        Project mockProject = Project.builder()
            .id(UUID.randomUUID())
            .name("Test Project")
            .build();
        Tag mockTag = Tag.builder()
            .id(tagId)
            .name("Bug")
            .color("#FF0000")
            .project(mockProject)
            .build();

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

        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.getTagById(tagId);
        });

        verify(tagRepository, times(1)).findById(tagId);
    }

    @Test
    void updateTag_Success() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        
        TagRequest request = new TagRequest("Updated Bug", "#00FF00", projectId);

        Project mockProject = Project.builder()
            .id(projectId)
            .name("Test Project")
            .build();

        User mockUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .role(Role.ADMIN)
            .build();

        Tag existingTag = Tag.builder()
            .id(tagId)
            .name("Old Bug")
            .color("#FF0000")
            .project(mockProject)
            .build();

        Tag updatedTag = Tag.builder()
            .id(tagId)
            .name("Updated Bug")
            .color("#00FF00")
            .project(mockProject)
            .build();

        try (MockedStatic<SecurityContextHolder> mockedSecurityHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            when(authentication.getName()).thenReturn(userId.toString());
            when(authentication.isAuthenticated()).thenReturn(true);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(tagRepository.findById(tagId)).thenReturn(Optional.of(existingTag));
            when(tagRepository.existsByNameAndProjectId(request.getName(), projectId)).thenReturn(false);
            when(tagRepository.save(any(Tag.class))).thenReturn(updatedTag);

            // ACT
            TagResponse response = tagService.updateTag(tagId, request);

            // ASSERT
            assertNotNull(response);
            assertEquals("Updated Bug", response.getName());
            assertEquals("#00FF00", response.getColor());
            assertEquals(projectId, response.getProjectId());

            verify(tagRepository, times(1)).save(any(Tag.class));
        }
    }

    @Test
    void updateTag_ThrowsException_WhenTagNotFound() {
        // ARRANGE
        UUID tagId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        
        TagRequest request = new TagRequest("Updated Bug", "#00FF00", projectId);
        
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.updateTag(tagId, request);
        });
    }

    @Test
    void updateTag_ThrowsException_WhenDuplicateNameInProject() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        
        TagRequest request = new TagRequest("Existing Tag", "#00FF00", projectId);

        Project mockProject = Project.builder()
            .id(projectId)
            .name("Test Project")
            .build();

        User mockUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .role(Role.ADMIN)
            .build();

        Tag existingTag = Tag.builder()
            .id(tagId)
            .name("Old Name")
            .color("#FF0000")
            .project(mockProject)
            .build();

        try (MockedStatic<SecurityContextHolder> mockedSecurityHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            when(authentication.getName()).thenReturn(userId.toString());
            when(authentication.isAuthenticated()).thenReturn(true);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(tagRepository.findById(tagId)).thenReturn(Optional.of(existingTag));
            when(tagRepository.existsByNameAndProjectId("Existing Tag", projectId)).thenReturn(true);

            // ACT & ASSERT
            TagDuplicateException exception = assertThrows(TagDuplicateException.class, () -> {
                tagService.updateTag(tagId, request);
            });

            assertTrue(exception.getMessage().contains("already exists"));
            verify(tagRepository, never()).save(any(Tag.class));
        }
    }

    @Test
    void deleteTag_Success() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        Project mockProject = Project.builder()
            .id(projectId)
            .name("Test Project")
            .build();

        User mockUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .role(Role.ADMIN)
            .build();

        Tag mockTag = Tag.builder()
            .id(tagId)
            .name("Bug")
            .color("#FF0000")
            .project(mockProject)
            .build();

        try (MockedStatic<SecurityContextHolder> mockedSecurityHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            when(authentication.getName()).thenReturn(userId.toString());
            when(authentication.isAuthenticated()).thenReturn(true);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(tagRepository.findById(tagId)).thenReturn(Optional.of(mockTag));

            // ACT
            tagService.deleteTag(tagId);

            // ASSERT
            verify(tagRepository, times(1)).deleteById(tagId);
        }
    }

    @Test
    void deleteTag_ThrowsException_WhenTagNotFound() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();

        User mockUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .role(Role.ADMIN)
            .build();

        try (MockedStatic<SecurityContextHolder> mockedSecurityHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            when(authentication.getName()).thenReturn(userId.toString());
            when(authentication.isAuthenticated()).thenReturn(true);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThrows(ResourceNotFoundException.class, () -> {
                tagService.deleteTag(tagId);
            });

            verify(tagRepository, never()).deleteById(any());
        }
    }

    @Test
    void createTag_ThrowsException_WhenUserNotMember() {
        // ARRANGE
        UUID userId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        TagRequest request = new TagRequest("Bug", "#FF0000", projectId);

        Project mockProject = Project.builder()
            .id(projectId)
            .name("Test Project")
            .build();

        User mockUser = User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .role(Role.EXTERNAL)
            .build();

        try (MockedStatic<SecurityContextHolder> mockedSecurityHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            when(authentication.getName()).thenReturn(userId.toString());
            when(authentication.isAuthenticated()).thenReturn(true);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedSecurityHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(projectRepository.existsByIdAndMembersId(projectId, userId)).thenReturn(false);

            // ACT & ASSERT
            assertThrows(UnauthorizedException.class, () -> {
                tagService.createTag(request);
            });

            verify(tagRepository, never()).save(any(Tag.class));
        }
    }

    @Test
    void updateTag_ThrowsException_WhenTagDoesNotBelongToProject() {
        // ARRANGE
        UUID projectId = UUID.randomUUID();
        UUID differentProjectId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        
        TagRequest request = new TagRequest("Updated Bug", "#00FF00", projectId);

        Project mockProject = Project.builder()
            .id(differentProjectId)
            .name("Different Project")
            .build();

        Tag existingTag = Tag.builder()
            .id(tagId)
            .name("Old Bug")
            .color("#FF0000")
            .project(mockProject)
            .build();

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(existingTag));

        // ACT & ASSERT
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            tagService.updateTag(tagId, request);
        });

        assertTrue(exception.getMessage().contains("does not belong"));
        verify(tagRepository, never()).save(any(Tag.class));
    }
}
