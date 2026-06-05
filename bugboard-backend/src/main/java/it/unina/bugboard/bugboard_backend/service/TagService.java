package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.TagRequest;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Tag;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ProjectRepository projectRepository;

    // Retrieve all tags of a specific project
    public List<TagResponse> getAllTagsByProjectId(UUID projectId) {
        return tagRepository
                .findByProjectId(projectId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Retrieve a single tag by its ID
    public TagResponse getTagById(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Tag with id %s not found", id)));
        return mapToResponse(tag);
    }

    // Create a new tag
    @Transactional
    public TagResponse createTag(TagRequest dto) {
        // 1. Verify that the project exists
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Project with id %s not found", dto.getProjectId())));
        // 2. Avoid duplicates
        if (tagRepository.existsByNameAndProjectId(dto.getName(), dto.getProjectId())) {
            throw new IllegalArgumentException(String.format("Tag '%s' already exists in this project", dto.getName()));
        }
        // 3. Build the entity
        Tag newTag = Tag.builder()
                .name(dto.getName())
                .color(dto.getColor())
                .project(project)
                .build();
        // 4. Save and return
        Tag savedTag = tagRepository.save(newTag);
        return mapToResponse(savedTag);
    }

    // Helper method to map the Entity to the DTO
    private TagResponse mapToResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .color(tag.getColor())
                .projectId(tag.getProject().getId())
                .build();
    }
}