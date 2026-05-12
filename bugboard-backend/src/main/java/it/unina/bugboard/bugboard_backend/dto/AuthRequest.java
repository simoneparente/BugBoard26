package it.unina.bugboard.bugboard_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class AuthRequest {
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private final String email;
    @NotBlank(message = "Password is required")
    private String password;
}
