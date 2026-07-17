package it.unina.bugboard.bugboard_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.exception.UnauthorizedException;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private UUID projectId;
    private UUID userId;

    private Project dummyProject;
    private User dummyUser;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        dummyProject = Project.builder()
                .id(projectId)
                .name("BugBoard Core")
                .description("test description")
                .build();

        dummyUser = User.builder()
                .id(userId)
                .username("tesuser")
                .email("test@example.com")
                .role(Role.ADMIN)
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(userId.toString());
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(userId)).thenReturn(Optional.of(dummyUser));
    }

    @Test
    void createProject_Success() {
        
        ProjectRequest request = ProjectRequest
                                .builder()
                                .name("BugBoard Core")
                                .description("test description")
                                .build();

        
        when(projectRepository.existsByName(request.getName())).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(dummyProject);

       
        Project result = projectService.createProject(request);

        assertNotNull(result);
        assertEquals("BugBoard Core", result.getName());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void createProject_ThrowsException_WhenNameExists() {
        
        ProjectRequest request = ProjectRequest
                                .builder()
                                .name("BugBoard Core")
                                .description("test description")
                                .build();

        
        when(projectRepository.existsByName(request.getName())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject(request);
        });

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
    void getProjects_ReturnsPage() {
        List<Project> projectList = List.of(dummyProject);
        Page<Project> projectPage = new PageImpl<>(projectList);
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.findAll(pageable)).thenReturn(projectPage);

        Page<Project> result = projectService.getProjects(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("BugBoard Core", result.getContent().get(0).getName());
    }

    @Test
    void deleteProject_Success() {
       doNothing().when(projectRepository).deleteById(projectId);

       projectService.deleteProject(projectId);

       verify(projectRepository, times(1)).deleteById(projectId);
   } 

  @Test
    void deleteProject_ThrowsException_WhenNotAdmin() {
        dummyUser.setRole(Role.TECHNICAL);
        when(userRepository.findById(userId)).thenReturn(Optional.of(dummyUser));

        assertThrows(UnauthorizedException.class, () -> {
            projectService.deleteProject(projectId);
        });

        verify(projectRepository, never()).deleteById(projectId);
    }

    @Test
    void getProjectById_Success_AsNonAdmin_WhenMember() {
        // Setup: change user role to TECHNICAL and is member
        dummyUser.setRole(Role.TECHNICAL);
        dummyProject.setMembers(List.of(dummyUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(dummyUser));
        when(projectRepository.existsByIdAndMembersId(projectId, userId)).thenReturn(true);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(dummyProject));

        // Act
        Project result = projectService.getProjectById(projectId);

        // Assert
        assertNotNull(result);
        assertEquals(projectId, result.getId());
        verify(projectRepository, times(1)).existsByIdAndMembersId(projectId, userId);
    }

    @Test
    void getProjectById_ThrowsException_WhenNonAdminAndNotMember() {
        // Setup: change user role to TECHNICAL and NOT a member
        dummyUser.setRole(Role.TECHNICAL);
        when(userRepository.findById(userId)).thenReturn(Optional.of(dummyUser));
        when(projectRepository.existsByIdAndMembersId(projectId, userId)).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            projectService.getProjectById(projectId);
        });

        verify(projectRepository, never()).findById(projectId);
    }

    @Test
    void getProjects_ReturnsAllProjects_AsAdmin() {
        // Setup: user is ADMIN
        List<Project> projectList = List.of(dummyProject);
        Page<Project> projectPage = new PageImpl<>(projectList);
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.findAll(pageable)).thenReturn(projectPage);

        // Act
        Page<Project> result = projectService.getProjects(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(projectRepository, times(1)).findAll(pageable);
        verify(projectRepository, never()).findByMembersId(any(), any());
    }

    @Test
    void getProjects_ReturnsOnlyMemberProjects_AsNonAdmin() {
        // Setup: change user role to TECHNICAL
        dummyUser.setRole(Role.TECHNICAL);
        when(userRepository.findById(userId)).thenReturn(Optional.of(dummyUser));
        
        List<Project> projectList = List.of(dummyProject);
        Page<Project> projectPage = new PageImpl<>(projectList);
        Pageable pageable = PageRequest.of(0, 10);
        when(projectRepository.findByMembersId(userId, pageable)).thenReturn(projectPage);

        // Act
        Page<Project> result = projectService.getProjects(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(projectRepository, times(1)).findByMembersId(userId, pageable);
        verify(projectRepository, never()).findAll(pageable);
    }

    @Test
    void deleteProject_ThrowsException_WhenUnauthenticated() {
        // Setup: no authentication
        SecurityContextHolder.clearContext();

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            projectService.deleteProject(projectId);
        });

        verify(projectRepository, never()).deleteById(projectId);
    }
}