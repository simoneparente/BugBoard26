package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.dto.ProjectRequest;
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
}