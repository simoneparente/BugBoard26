package it.unina.bugboard.bugboard_backend.dto;

import it.unina.bugboard.bugboard_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Data //Automatically generate getters, setters, equals, toString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private Role role;
}
