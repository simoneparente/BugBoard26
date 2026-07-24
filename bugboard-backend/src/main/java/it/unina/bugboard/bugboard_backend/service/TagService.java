package it.unina.bugboard.bugboard_backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.unina.bugboard.bugboard_backend.dto.TagRequest;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Role;
import it.unina.bugboard.bugboard_backend.entity.Tag;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.exception.TagDuplicateException;
import it.unina.bugboard.bugboard_backend.exception.UnauthorizedException;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.repository.TagRepository;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * Retrieve all tags of a specific project by key.
     */
    public List<TagResponse> getAllTagsByProjectKey(String projectKey) {
        return tagRepository
                .findByProjectKey(projectKey).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieve a single tag by its ID.
     */
    public TagResponse getTagById(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Tag with id %s not found", id)));
        return mapToResponse(tag);
    }

    /**
     * Create a new tag. Only ADMIN or project members can create tags.
     */
    @Transactional
    public TagResponse createTag(TagRequest dto) {
        // 1. Get current project
        Project project = projectRepository.findByKey(dto.getProjectKey())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Project with key %s not found", dto.getProjectKey())));

        // 2. Get current user and validate authorization
        User currentUser = getCurrentUser();
        validateUserIsMemberOrAdmin(currentUser, project.getId());

        // 3. Avoid duplicates
        if (tagRepository.existsByNameAndProjectKey(dto.getName(), dto.getProjectKey())) {
            throw new TagDuplicateException(String.format("Tag '%s' already exists in this project", dto.getName()));
        }

        // 4. Build the entity
        Tag newTag = Tag.builder()
                .name(dto.getName())
                .color(dto.getColor())
                .project(project)
                .build();

        // 5. Save and return
        Tag savedTag = tagRepository.save(newTag);
        return mapToResponse(savedTag);
    }

    /**
     * Update an existing tag. Only ADMIN or project members can update tags.
     * Authorization check is performed against the tag's actual project, not the DTO's project ID.
     */
    @Transactional
    public TagResponse updateTag(UUID tagId, TagRequest dto) {
        // 1. Retrieve the tag first
        Tag existingTag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Tag with id %s not found", tagId)));

        // 2. Verify that the tag belongs to the requested project (prevents tag hijacking)
        if (!existingTag.getProject().getKey().equals(dto.getProjectKey())) {
            throw new IllegalArgumentException("Tag does not belong to the specified project");
        }

        // 3. Get current user and validate authorization against the tag's actual project
        User currentUser = getCurrentUser();
        validateUserIsMemberOrAdmin(currentUser, existingTag.getProject().getId());

        // 4. Check for name duplicate (excluding current tag)
        if (!existingTag.getName().equals(dto.getName()) &&
                tagRepository.existsByNameAndProjectKey(dto.getName(), dto.getProjectKey())) {
            throw new TagDuplicateException(String.format("Tag '%s' already exists in this project", dto.getName()));
        }

        // 5. Update the tag
        existingTag.setName(dto.getName());
        existingTag.setColor(dto.getColor());

        // 6. Save and return
        Tag updatedTag = tagRepository.save(existingTag);
        return mapToResponse(updatedTag);
    }

    /**
     * Delete a tag. Only ADMIN or project members can delete tags.
     * Clears all issue associations before deletion to prevent foreign key constraint violations.
     */
    @Transactional
    public void deleteTag(UUID tagId) {
        // 1. Get current user
        User currentUser = getCurrentUser();

        // 2. Retrieve the tag
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Tag with id %s not found", tagId)));

        // 3. Validate authorization for this project
        validateUserIsMemberOrAdmin(currentUser, tag.getProject().getId());

        // 4. Remove all issue associations from the join table before deletion
        tagRepository.removeTagFromAllIssues(tagId);

        // 5. Delete the tag
        tagRepository.deleteById(tagId);
    }

    /**
     * Helper method to map the Entity to the DTO.
     */
    private TagResponse mapToResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .projectKey(tag.getProject().getKey())
                .build();
    }

    /**
     * Get the current authenticated user from SecurityContext.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated.");
        }

        String userIdString = authentication.getName();
        UUID userId;
        try {
            userId = UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid user ID format.");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found."));
    }

    /**
     * Validate that the user is either ADMIN or a member of the project.
     */
    private void validateUserIsMemberOrAdmin(User user, UUID projectId) {
        // ADMIN can always manage tags
        if (user.getRole() == Role.ADMIN) {
            return;
        }

        // For non-ADMIN users, check if they are members of the project
        boolean isMember = projectRepository.existsByIdAndMembersId(projectId, user.getId());
        if (!isMember) {
            throw new UnauthorizedException("You must be a member of this project to manage tags.");
        }
    }
}