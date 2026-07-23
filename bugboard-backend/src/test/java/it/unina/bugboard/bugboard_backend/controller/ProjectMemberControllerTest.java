package it.unina.bugboard.bugboard_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.unina.bugboard.bugboard_backend.dto.AddProjectMembersRequest;
import it.unina.bugboard.bugboard_backend.dto.UserResponse;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.exception.UnauthorizedException;
import it.unina.bugboard.bugboard_backend.mapper.UserMapper;
import it.unina.bugboard.bugboard_backend.service.ProjectService;

@ExtendWith(MockitoExtension.class)
class ProjectMemberControllerTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ProjectController projectController;

    private UUID projectId;
    private UUID userId;
    private User testUser;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .role(Role.TECHNICAL)
                .build();

        testUserResponse = UserResponse.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .role(Role.TECHNICAL)
                .build();
    }

    @Test
    void testGetProjectMembers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> membersPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(projectService.getProjectMembers(projectId, pageable)).thenReturn(membersPage);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        ResponseEntity<Page<UserResponse>> response = projectController.getProjectMembers(projectId, pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testGetAvailableUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> availableUsersPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(projectService.getAvailableUsers(projectId, pageable)).thenReturn(availableUsersPage);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        ResponseEntity<Page<UserResponse>> response = projectController.getAvailableUsers(projectId, pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void testAddMembersToProject_Success() {
        AddProjectMembersRequest request = AddProjectMembersRequest.builder()
                .userIds(List.of(userId))
                .build();

        when(projectService.addMembersToProject(projectId, request.getUserIds()))
                .thenReturn(List.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        ResponseEntity<List<UserResponse>> response = projectController.addMembersToProject(projectId, request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(projectService, times(1)).addMembersToProject(projectId, request.getUserIds());
    }

    @Test
    void testAddMembersToProject_Unauthorized() {
        AddProjectMembersRequest request = AddProjectMembersRequest.builder()
                .userIds(List.of(userId))
                .build();

        when(projectService.addMembersToProject(projectId, request.getUserIds()))
                .thenThrow(new UnauthorizedException("Only ADMIN users can add members"));

        assertThrows(UnauthorizedException.class, () ->
                projectController.addMembersToProject(projectId, request)
        );
    }

    @Test
    void testAddMembersToProject_RejectsAdminUsers() {
        AddProjectMembersRequest request = AddProjectMembersRequest.builder()
                .userIds(List.of(userId))
                .build();

        when(projectService.addMembersToProject(projectId, request.getUserIds()))
                .thenThrow(new UnauthorizedException("ADMIN users cannot be added as project members. Only TECHNICAL and EXTERNAL users are allowed."));

        assertThrows(UnauthorizedException.class, () ->
                projectController.addMembersToProject(projectId, request)
        );
    }

    @Test
    void testAddMembersToProject_EmptyUserIds() {
        AddProjectMembersRequest request = AddProjectMembersRequest.builder()
                .userIds(List.of())
                .build();

        assertNotNull(request.getUserIds());
        assertTrue(request.getUserIds().isEmpty());
    }
}
