package it.unina.bugboard.bugboard_backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.unina.bugboard.bugboard_backend.dto.TagRequest;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * Endpoint to create a new tag.
     * POST /api/tags
     */
    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagRequest request) {
        TagResponse response = tagService.createTag(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint to retrieve a single tag by its ID.
     * GET /api/tags/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable UUID id) {
        TagResponse response = tagService.getTagById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to retrieve all tags belonging to a specific project.
     * GET /api/tags/project/{projectKey}
     */
    @GetMapping("/project/{projectKey}")
    public ResponseEntity<List<TagResponse>> getTagsByProjectKey(@PathVariable String projectKey) {
        List<TagResponse> response = tagService.getAllTagsByProjectKey(projectKey);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to update an existing tag.
     * PUT /api/tags/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody TagRequest request) {
        TagResponse response = tagService.updateTag(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to delete a tag by its ID.
     * DELETE /api/tags/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}