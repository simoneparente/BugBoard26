package it.unina.bugboard.bugboard_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.exception.UnauthorizedException;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectService projectService;

    private User adminUser;
    private User technicalUser1;
    private User technicalUser2;
    private Project testProject;
    private UUID projectId;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        UUID techId1 = UUID.randomUUID();
        UUID techId2 = UUID.randomUUID();
        projectId = UUID.randomUUID();

        adminUser = User.builder()
                .id(adminId)
                .username("admin")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        technicalUser1 = User.builder()
                .id(techId1)
                .username("tech1")
                .email("tech1@example.com")
                .role(Role.TECHNICAL)
                .build();

        technicalUser2 = User.builder()
                .id(techId2)
                .username("tech2")
                .email("tech2@example.com")
                .role(Role.TECHNICAL)
                .build();

        testProject = Project.builder()
                .id(projectId)
                .name("Test Project")
                .description("Test")
                .members(new ArrayList<>(List.of(adminUser, technicalUser1)))
                .build();
    }

    private void setupSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(adminId.toString());
    }

    @Test
    void testGetProjectMembers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> memberPage = new PageImpl<>(List.of(adminUser, technicalUser1), pageable, 2);
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.findMembersByProjectId(projectId, pageable)).thenReturn(memberPage);

        Page<User> result = projectService.getProjectMembers(projectId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(u -> u.getId().equals(technicalUser1.getId())));
    }

    @Test
    void testGetAvailableUsers_ExcludesCurrentMembersAndAdmins() {
        User anotherAdmin = User.builder()
                .id(UUID.randomUUID())
                .username("admin2")
                .email("admin2@example.com")
                .role(Role.ADMIN)
                .build();
        
        List<User> availableUsers = List.of(technicalUser2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> availablePage = new PageImpl<>(availableUsers, pageable, availableUsers.size());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.findAvailableUsersForProject(projectId, pageable)).thenReturn(availablePage);

        Page<User> result = projectService.getAvailableUsers(projectId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(technicalUser2.getId(), result.getContent().get(0).getId());
        // Verify no ADMIN users in result
        assertTrue(result.getContent().stream().noneMatch(u -> u.getRole() == Role.ADMIN));
    }

    @Test
    void testAddMembersToProject_OnlyAdminCanAdd() {
        setupSecurityContext();
        User technicalUser = User.builder()
                .id(UUID.randomUUID())
                .username("tech")
                .email("tech@example.com")
                .role(Role.TECHNICAL)
                .build();

        when(authentication.getName()).thenReturn(technicalUser.getId().toString());
        when(userRepository.findById(technicalUser.getId())).thenReturn(Optional.of(technicalUser));

        assertThrows(UnauthorizedException.class, () ->
                projectService.addMembersToProject(projectId, List.of(technicalUser2.getId()))
        );
    }

    @Test
    void testAddMembersToProject_Success() {
        setupSecurityContext();
        List<UUID> userIdsToAdd = List.of(technicalUser2.getId());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(userRepository.findAllById(userIdsToAdd)).thenReturn(List.of(technicalUser2));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        List<User> result = projectService.addMembersToProject(projectId, userIdsToAdd);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(technicalUser2.getId(), result.get(0).getId());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testAddMembersToProject_ExcludesDuplicates() {
        setupSecurityContext();
        List<UUID> userIdsToAdd = List.of(technicalUser1.getId(), technicalUser2.getId());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(userRepository.findAllById(userIdsToAdd)).thenReturn(List.of(technicalUser1, technicalUser2));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        List<User> result = projectService.addMembersToProject(projectId, userIdsToAdd);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(technicalUser2.getId(), result.get(0).getId());
    }

    @Test
    void testAddMembersToProject_UserNotFound() {
        setupSecurityContext();
        List<UUID> userIdsToAdd = List.of(UUID.randomUUID());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(userRepository.findAllById(userIdsToAdd)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () ->
                projectService.addMembersToProject(projectId, userIdsToAdd)
        );
    }

    @Test
    void testAddMembersToProject_RejectsAdminUsers() {
        setupSecurityContext();
        UUID anotherAdminId = UUID.randomUUID();
        User anotherAdmin = User.builder()
                .id(anotherAdminId)
                .username("admin2")
                .email("admin2@example.com")
                .role(Role.ADMIN)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(userRepository.findAllById(List.of(anotherAdminId))).thenReturn(List.of(anotherAdmin));

        assertThrows(UnauthorizedException.class, () ->
                projectService.addMembersToProject(projectId, List.of(anotherAdminId))
        );
    }

    @Test
    void testAddMembersToProject_AllAlreadyMembers() {
        setupSecurityContext();
        List<UUID> userIdsToAdd = List.of(technicalUser1.getId());

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(userRepository.findAllById(userIdsToAdd)).thenReturn(List.of(technicalUser1));

        assertThrows(IllegalArgumentException.class, () ->
                projectService.addMembersToProject(projectId, userIdsToAdd)
        );
    }
}
