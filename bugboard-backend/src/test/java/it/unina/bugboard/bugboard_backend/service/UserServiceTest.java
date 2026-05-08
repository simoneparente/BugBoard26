package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.UserRegistrationRequest;
import it.unina.bugboard.bugboard_backend.dto.UserResponse;
import it.unina.bugboard.bugboard_backend.entity.Invitation;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.exception.InvalidInvitationException;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.InvitationRepository;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private InvitationRepository invitationRepository;

    @InjectMocks
    private UserService userService;


    @Test
    void getAllUsers_Success3Users() {
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .email("user1@example.com")
                .passwordHash("hash1")
                .role(Role.TECHNICAL)
                .build();

        User user2 = User.builder()
                .id(UUID.randomUUID())
                .username("user2")
                .email("user2@example.com")
                .passwordHash("hash2")
                .role(Role.ADMIN)
                .build();

        User user3 = User.builder()
                .id(UUID.randomUUID())
                .username("user3")
                .email("user3@example.com")
                .passwordHash("hash3")
                .role(Role.EXTERNAL)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));

        List<UserResponse> responseList = userService.getAllUsers();

        assertNotNull(responseList);
        assertEquals(3, responseList.size());
        assertEquals("user1", responseList.get(0).getUsername());
        assertEquals("user2", responseList.get(1).getUsername());
        assertEquals("user3", responseList.get(2).getUsername());
        assertEquals("user1@example.com", responseList.get(0).getEmail());
        assertEquals("user2@example.com", responseList.get(1).getEmail());
        assertEquals("user3@example.com", responseList.get(2).getEmail());
        assertEquals(Role.TECHNICAL, responseList.get(0).getRole());
        assertEquals(Role.ADMIN, responseList.get(1).getRole());
        assertEquals(Role.EXTERNAL, responseList.get(2).getRole());

        verify(userRepository).findAll();
    }

    @Test
    void getUserById_Success() {
        UUID uuid = UUID.randomUUID();

        User user = User.builder()
                .id(uuid)
                .username("test")
                .email("test@example.com")
                .passwordHash("hash")
                .role(Role.TECHNICAL)
                .build();

        when(userRepository.findById(uuid)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(uuid);

        assertNotNull(response);
        assertEquals(uuid, response.getId());
        assertEquals("test", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(Role.TECHNICAL, response.getRole());

        verify(userRepository).findById(uuid);
    }

    @Test
    void getUserById_NotFound() {
        UUID uuid = UUID.randomUUID();
        when(userRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(uuid));
        verify(userRepository).findById(uuid);
    }

    @Test
    void getUserById_NotFound_WithMessage() {
        UUID uuid = UUID.randomUUID();

        when(userRepository.findById(uuid)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(uuid)
        );

        assertTrue(ex.getMessage().contains(uuid.toString()));
    }

    @Test
    void registerUser_Success_HashesPasswordAndSavesUser() {
        UserRegistrationRequest request = new UserRegistrationRequest(
                "token", "username", "email@test.com", "password123"
        );

        Invitation mockInvitation = getMockInvitation();

        when(invitationRepository.findByToken("token")).thenReturn(Optional.of(mockInvitation));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash_sicuro_123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Simula il comportamento di save restituendo l'utente passato come argomento

        UserResponse response = userService.registerUser(request);
        assertNotNull(response);
        assertEquals("username", response.getUsername());
        assertEquals("email@test.com", response.getEmail());
        assertEquals(Role.TECHNICAL, response.getRole());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(invitationRepository).delete(mockInvitation);

        User capturedUser = userCaptor.getValue();
        assertEquals("hash_sicuro_123", capturedUser.getPasswordHash());
        assertNotEquals("password123", capturedUser.getPasswordHash());
    }


    @Test
    void registerUser_UsernameAlreadyExists() {
        UserRegistrationRequest request = getRegistrationRequest();
        Invitation mockInvitation = getMockInvitation();
        when(invitationRepository.findByToken("token")).thenReturn(Optional.of(mockInvitation));
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        UserRegistrationRequest request = getRegistrationRequest();
        Invitation mockInvitation = getMockInvitation();

        when(invitationRepository.findByToken("token")).thenReturn(Optional.of(mockInvitation));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));
    }

    @Test
    void registerUser_ThrowsInvalidInvitationException_WhenTokenNotFound() {
        UserRegistrationRequest request = getRegistrationRequest();
        when(invitationRepository.findByToken("token")).thenReturn(Optional.empty());
        assertThrows(InvalidInvitationException.class, () -> userService.registerUser(request));
    }

    @Test
    void registerUser_ThrowsInvalidInvitationException_WhenTokenIsExpired() {
        UserRegistrationRequest request = getRegistrationRequest();
        Invitation mockInvitation = getExpiredMockInvitation();
        when(invitationRepository.findByToken("token")).thenReturn(Optional.of(mockInvitation));

        assertThrows(InvalidInvitationException.class, () -> userService.registerUser(request));
        verify(invitationRepository).delete(mockInvitation);
    }

    private static @NonNull UserRegistrationRequest getRegistrationRequest() {
        return new UserRegistrationRequest(
                "token", "username", "email@test.com", "password123"
        );
    }
    private static Invitation getMockInvitation() {
        return Invitation.builder()
                .token("token")
                .role(Role.TECHNICAL)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    private static Invitation getExpiredMockInvitation() {
        return Invitation.builder()
                .token("token")
                .role(Role.TECHNICAL)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
    }


}
