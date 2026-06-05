package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.TagRequest;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // Endpoint per la creazione di un nuovo tag
    // Risponde a: POST http://localhost:8080/api/tags
    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagRequest request) {
        TagResponse response = tagService.createTag(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Endpoint per recuperare un singolo tag tramite il suo ID
    // Risponde a: GET http://localhost:8080/api/tags/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable UUID id) {
        TagResponse response = tagService.getTagById(id);
        return ResponseEntity.ok(response);
    }

    // Endpoint per recuperare tutti i tag appartenenti a uno specifico progetto
    // Risponde a: GET http://localhost:8080/api/tags/project/{projectId}
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TagResponse>> getTagsByProjectId(@PathVariable UUID projectId) {
        List<TagResponse> response = tagService.getAllTagsByProjectId(projectId);
        return ResponseEntity.ok(response);
    }
}