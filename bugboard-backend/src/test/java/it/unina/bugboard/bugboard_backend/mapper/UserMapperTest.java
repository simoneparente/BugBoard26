package it.unina.bugboard.bugboard_backend.mapper;

import it.unina.bugboard.bugboard_backend.dto.UserResponse;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toResponse_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .role(Role.ADMIN)
                .build();

        // Act
        UserResponse response = userMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    void toResponse_ReturnsNull_WhenUserIsNull() {
        // Act
        UserResponse response = userMapper.toResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void toResponse_WithDifferentRoles() {
        // Test with USER role
        User userWithRole = User.builder()
                .id(UUID.randomUUID())
                .username("regularuser")
                .email("regular@example.com")
                .role(Role.TECHNICAL)
                .build();

        UserResponse response = userMapper.toResponse(userWithRole);

        assertNotNull(response);
        assertEquals(Role.TECHNICAL, response.getRole());
    }
}
