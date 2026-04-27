package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.UserRegistrationRequest;
import it.unina.bugboard.bugboard_backend.dto.UserResponse;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
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

    public UserResponse registerUser(UserRegistrationRequest dto) {
        /*TODO: When we have the Invitation module, here we will look for the token in the DB.
        If token does not exist, throw ResourceNotFoundException 
        For now, we will just create the user without checking the token */
        if(userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already in use");
        }
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User newUser = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                //TODO: For now, all registered users will have the TECHNICAL role. In the future, we will assign roles based on the invitation token.
                .role(Role.TECHNICAL)
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .build();

        User savedUser = userRepository.save(newUser);
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
