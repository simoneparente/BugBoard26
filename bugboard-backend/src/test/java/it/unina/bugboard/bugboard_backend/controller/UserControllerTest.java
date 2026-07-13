package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.UserRegistrationRequest;
import it.unina.bugboard.bugboard_backend.dto.UserResponse;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.service.UserService;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UUID userId;
    private UserResponse testUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new UserResponse(userId, "testuser", "test@example.com", Role.TECHNICAL);
    }

    @Test
    void getAllUsers_WithDefaultPageable_ReturnsOkAndPagedUsers() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        UserResponse user1 = new UserResponse(id1, "user1", "user1@example.com", Role.TECHNICAL);
        UserResponse user2 = new UserResponse(id2, "user2", "user2@example.com", Role.ADMIN);

        Page<UserResponse> pagedResponse = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 10), 2);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(pagedResponse);

        Pageable pageable = PageRequest.of(0, 10);
        ResponseEntity<Page<UserResponse>> response = userController.getAllUsers(pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals("user1", response.getBody().getContent().get(0).getUsername());
        assertEquals("user2", response.getBody().getContent().get(1).getUsername());

        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    void getAllUsers_WithCustomPagination_ReturnsOkAndPagedUsers() {
        UUID id1 = UUID.randomUUID();
        UserResponse user1 = new UserResponse(id1, "user1", "user1@example.com", Role.TECHNICAL);

        Page<UserResponse> pagedResponse = new PageImpl<>(List.of(user1), PageRequest.of(1, 5), 10);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(pagedResponse);

        Pageable pageable = PageRequest.of(1, 5);
        ResponseEntity<Page<UserResponse>> response = userController.getAllUsers(pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(10, response.getBody().getTotalElements());

        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    void getAllUsers_EmptyPage_ReturnsOkAndEmptyPage() {
        Page<UserResponse> pagedResponse = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(pagedResponse);

        Pageable pageable = PageRequest.of(0, 10);
        ResponseEntity<Page<UserResponse>> response = userController.getAllUsers(pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getTotalElements());

        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    void getUserById_WithValidId_ReturnsOkAndUser() {
        when(userService.getUserById(userId)).thenReturn(testUser);

        ResponseEntity<UserResponse> response = userController.getUserById(userId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testuser", response.getBody().getUsername());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals(Role.TECHNICAL, response.getBody().getRole());

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void getUserById_WithInvalidId_ThrowsResourceNotFoundException() {
        UUID invalidId = UUID.randomUUID();
        when(userService.getUserById(invalidId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        assertThrows(ResourceNotFoundException.class, () -> userController.getUserById(invalidId));

        verify(userService, times(1)).getUserById(invalidId);
    }

    @Test
    void getUserById_VerifiesServiceCall() {
        when(userService.getUserById(userId)).thenReturn(testUser);

        userController.getUserById(userId);

        verify(userService).getUserById(userId);
    }

    @Test
    void registerUser_WithValidRequest_Returns201AndUser() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "valid-token",
                "newuser",
                "newuser@example.com",
                "securePassword123"
        );

        UUID newUserId = UUID.randomUUID();
        UserResponse response = new UserResponse(newUserId, "newuser", "newuser@example.com", Role.TECHNICAL);

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(response);

        ResponseEntity<UserResponse> httpResponse = userController.registerUser(request);

        assertNotNull(httpResponse);
        assertEquals(HttpStatus.CREATED, httpResponse.getStatusCode());
        assertEquals("newuser", httpResponse.getBody().getUsername());
        assertEquals("newuser@example.com", httpResponse.getBody().getEmail());
        assertEquals(Role.TECHNICAL, httpResponse.getBody().getRole());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void registerUser_WithDifferentRoles_Returns201() {
        UserRegistrationRequest adminRequest = new UserRegistrationRequest(
                "admin-token",
                "adminuser",
                "admin@example.com",
                "password123"
        );

        UUID newUserId = UUID.randomUUID();
        UserResponse adminResponse = new UserResponse(newUserId, "adminuser", "admin@example.com", Role.ADMIN);

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(adminResponse);

        ResponseEntity<UserResponse> response = userController.registerUser(adminRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Role.ADMIN, response.getBody().getRole());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void registerUser_WithExternalRole_Returns201() {
        UserRegistrationRequest externalRequest = new UserRegistrationRequest(
                "external-token",
                "externaluser",
                "external@example.com",
                "password123"
        );

        UUID newUserId = UUID.randomUUID();
        UserResponse externalResponse = new UserResponse(newUserId, "externaluser", "external@example.com", Role.EXTERNAL);

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(externalResponse);

        ResponseEntity<UserResponse> response = userController.registerUser(externalRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Role.EXTERNAL, response.getBody().getRole());

        verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
    }

    @Test
    void registerUser_VerifiesServiceCall() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "token",
                "user",
                "user@example.com",
                "password"
        );

        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(testUser);

        userController.registerUser(request);

        verify(userService).registerUser(any(UserRegistrationRequest.class));
    }
}
