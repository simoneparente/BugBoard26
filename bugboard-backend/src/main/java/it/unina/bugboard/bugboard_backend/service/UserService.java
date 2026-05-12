package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.UserRegistrationRequest;
import it.unina.bugboard.bugboard_backend.dto.UserResponse;
import it.unina.bugboard.bugboard_backend.entity.Invitation;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.exception.InvalidInvitationException;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.InvitationRepository;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository
                .findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with id %s not found", id)));
        return mapToResponse(user);
    }

    @Transactional
    // Transactional to ensure atomicity of the registration process,
    // registration is successful only if the invitation is valid and the user is created, 
    // otherwise any changes are rolled back
    public UserResponse registerUser(UserRegistrationRequest dto) {
        Invitation invitation = invitationRepository.findByToken(dto.getToken())
                .orElseThrow(() -> new InvalidInvitationException("Invalid invitation token"));

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitationRepository.delete(invitation);
            throw new InvalidInvitationException("Invitation expired.");
        }
        if(userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already in use");
        }
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User newUser = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .role(invitation.getRole())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .build();

        User savedUser = userRepository.save(newUser);
        invitationRepository.delete(invitation);
        return mapToResponse(savedUser);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
                );
    }
}
