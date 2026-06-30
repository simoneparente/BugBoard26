package it.unina.bugboard.bugboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import it.unina.bugboard.bugboard_backend.entity.Role;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String username;
    private String email;
    private Role role;
}
