package it.unina.bugboard.bugboard_backend.dto;
import it.unina.bugboard.bugboard_backend.entity.Role;

public record AuthResult(String token, String username, String email, Role role) {}