package it.unina.bugboard.bugboard_backend.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Nested;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
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

    @Mock
    private it.unina.bugboard.bugboard_backend.repository.IssueRepository issueRepository;

    @Mock
    private it.unina.bugboard.bugboard_backend.mapper.UserMapper userMapper;

    @InjectMocks
    private ProjectService projectService;

    private UUID projectId;
    private UUID userId;

    private Project dummyProject;
    private User dummyUser;

    protected void setupAuthenticatedContext() {
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
        lenient().when(authentication.getName()).thenReturn(userId.toString());
        lenient().when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(dummyUser));
    }

    @Nested
    class AuthenticatedUserTests {
        @BeforeEach
        void setUp() {
            setupAuthenticatedContext();
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
        void getRecommendedAssignees_Success() {
            User user1 = User.builder().id(UUID.randomUUID()).username("user1").role(Role.TECHNICAL).build();
            User user2 = User.builder().id(UUID.randomUUID()).username("user2").role(Role.TECHNICAL).build();

            dummyProject.setMembers(List.of(user1, user2));
            when(projectRepository.findByIdWithMembers(projectId)).thenReturn(Optional.of(dummyProject));

            it.unina.bugboard.bugboard_backend.entity.Issue highPriorityIssue = it.unina.bugboard.bugboard_backend.entity.Issue.builder()
                    .assignee(user1)
                    .priority(it.unina.bugboard.bugboard_backend.entity.IssuePriority.HIGH) // weight 4
                    .status(it.unina.bugboard.bugboard_backend.entity.IssueStatus.IN_PROGRESS)
                    .build();

            when(issueRepository.findByProjectIdAndStatusNotIn(eq(projectId), any()))
                    .thenReturn(List.of(highPriorityIssue));

            when(userMapper.toResponse(user1)).thenReturn(it.unina.bugboard.bugboard_backend.dto.UserResponse.builder().id(user1.getId()).username("user1").build());
            when(userMapper.toResponse(user2)).thenReturn(it.unina.bugboard.bugboard_backend.dto.UserResponse.builder().id(user2.getId()).username("user2").build());

            List<it.unina.bugboard.bugboard_backend.dto.AssigneeRecommendationResponse> recommendations = projectService.getRecommendedAssignees(projectId);

            assertNotNull(recommendations);
            assertEquals(2, recommendations.size());
            // user2 has 0 workload, user1 has 4 workload -> user2 must be recommended first
            assertEquals("user2", recommendations.get(0).getUser().getUsername());
            assertEquals(0, recommendations.get(0).getWorkloadScore());
            assertEquals("user1", recommendations.get(1).getUser().getUsername());
            assertEquals(4, recommendations.get(1).getWorkloadScore());
        }

        @Test
        void getRecommendedAssignees_EmptyOrNullMembers_ReturnsEmptyList() {
            dummyProject.setMembers(List.of());
            when(projectRepository.findByIdWithMembers(projectId)).thenReturn(Optional.of(dummyProject));

            List<it.unina.bugboard.bugboard_backend.dto.AssigneeRecommendationResponse> recommendations = projectService.getRecommendedAssignees(projectId);

            assertNotNull(recommendations);
            assertEquals(0, recommendations.size());

            dummyProject.setMembers(null);
            List<it.unina.bugboard.bugboard_backend.dto.AssigneeRecommendationResponse> nullMembersRecs = projectService.getRecommendedAssignees(projectId);
            assertNotNull(nullMembersRecs);
            assertEquals(0, nullMembersRecs.size());
        }

        @Test
        void getRecommendedAssignees_IssueWithNullPriorityAndNullAssignee() {
            User user1 = User.builder().id(UUID.randomUUID()).username("user1").role(Role.TECHNICAL).build();
            dummyProject.setMembers(List.of(user1));
            when(projectRepository.findByIdWithMembers(projectId)).thenReturn(Optional.of(dummyProject));

            it.unina.bugboard.bugboard_backend.entity.Issue nullPriorityIssue = it.unina.bugboard.bugboard_backend.entity.Issue.builder()
                    .assignee(user1)
                    .priority(null)
                    .status(it.unina.bugboard.bugboard_backend.entity.IssueStatus.IN_PROGRESS)
                    .build();

            it.unina.bugboard.bugboard_backend.entity.Issue unassignedIssue = it.unina.bugboard.bugboard_backend.entity.Issue.builder()
                    .assignee(null)
                    .priority(it.unina.bugboard.bugboard_backend.entity.IssuePriority.HIGH)
                    .status(it.unina.bugboard.bugboard_backend.entity.IssueStatus.IN_PROGRESS)
                    .build();

            when(issueRepository.findByProjectIdAndStatusNotIn(eq(projectId), any()))
                    .thenReturn(List.of(nullPriorityIssue, unassignedIssue));

            when(userMapper.toResponse(user1)).thenReturn(it.unina.bugboard.bugboard_backend.dto.UserResponse.builder().id(user1.getId()).username("user1").build());

            List<it.unina.bugboard.bugboard_backend.dto.AssigneeRecommendationResponse> recommendations = projectService.getRecommendedAssignees(projectId);

            assertNotNull(recommendations);
            assertEquals(1, recommendations.size());
            assertEquals(1, recommendations.get(0).getWorkloadScore());
        }

        @Test
        void addMemberToProject_Success() {
            UUID memberUserId = UUID.randomUUID();
            User newMember = User.builder().id(memberUserId).username("newmember").build();
            dummyProject.setMembers(new java.util.ArrayList<>());

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(dummyProject));
            when(userRepository.findById(memberUserId)).thenReturn(Optional.of(newMember));
            when(projectRepository.save(dummyProject)).thenReturn(dummyProject);

            Project result = projectService.addMemberToProject(projectId, memberUserId);

            assertNotNull(result);
            assertEquals(1, result.getMembers().size());
            verify(projectRepository, times(1)).save(dummyProject);
        }

        @Test
        void addMemberToProject_AlreadyMember_DoesNotDuplicate() {
            UUID memberUserId = UUID.randomUUID();
            User newMember = User.builder().id(memberUserId).username("existingmember").build();
            dummyProject.setMembers(new java.util.ArrayList<>(List.of(newMember)));

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(dummyProject));
            when(userRepository.findById(memberUserId)).thenReturn(Optional.of(newMember));
            when(projectRepository.save(dummyProject)).thenReturn(dummyProject);

            Project result = projectService.addMemberToProject(projectId, memberUserId);

            assertNotNull(result);
            assertEquals(1, result.getMembers().size());
            verify(projectRepository, times(1)).save(dummyProject);
        }

        @Test
        void addMemberToProject_ThrowsException_WhenUserNotFound() {
            UUID memberUserId = UUID.randomUUID();

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(dummyProject));
            when(userRepository.findById(memberUserId)).thenReturn(Optional.empty());

            assertThrows(it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException.class, () -> {
                projectService.addMemberToProject(projectId, memberUserId);
            });
        }

        @Test
        void removeMemberFromProject_Success() {
            UUID memberUserId = UUID.randomUUID();
            User memberToRemove = User.builder().id(memberUserId).username("membertoremove").build();
            dummyProject.setMembers(new java.util.ArrayList<>(List.of(memberToRemove)));

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(dummyProject));
            when(userRepository.findById(memberUserId)).thenReturn(Optional.of(memberToRemove));
            when(projectRepository.save(dummyProject)).thenReturn(dummyProject);

            Project result = projectService.removeMemberFromProject(projectId, memberUserId);

            assertNotNull(result);
            assertEquals(0, result.getMembers().size());
            verify(projectRepository, times(1)).save(dummyProject);
        }

        @Test
        void removeMemberFromProject_ThrowsException_WhenUserNotFound() {
            UUID memberUserId = UUID.randomUUID();

            when(projectRepository.findById(projectId)).thenReturn(Optional.of(dummyProject));
            when(userRepository.findById(memberUserId)).thenReturn(Optional.empty());

            assertThrows(it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException.class, () -> {
                projectService.removeMemberFromProject(projectId, memberUserId);
            });
        }
    }

    @Nested
    class UnauthenticatedUserTests {
        @BeforeEach
        void setUp() {
            projectId = UUID.randomUUID();
            dummyProject = Project.builder()
                    .id(projectId)
                    .name("BugBoard Core")
                    .description("test description")
                    .build();
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
}