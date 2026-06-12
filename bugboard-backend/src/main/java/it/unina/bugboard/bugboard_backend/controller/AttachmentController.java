package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.AttachmentResponse;
import it.unina.bugboard.bugboard_backend.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    // Endpoint to upload a file attached to an Issue
    // Responds to: POST http://localhost:8080/api/attachments/issue/{issueId}
    @PostMapping(value = "/issue/{issueId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable UUID issueId,
            @RequestParam("file") MultipartFile file) {
        
        AttachmentResponse response = attachmentService.uploadAttachment(issueId, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Endpoint to retrieve the metadata of a single attachment
    // Responds to: GET http://localhost:8080/api/attachments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AttachmentResponse> getAttachmentById(@PathVariable UUID id) {
        AttachmentResponse response = attachmentService.getAttachmentById(id);
        return ResponseEntity.ok(response);
    }

    // Endpoint to retrieve all attachments belonging to a specific Issue
    // Responds to: GET http://localhost:8080/api/attachments/issue/{issueId}
    @GetMapping("/issue/{issueId}")
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsByIssueId(@PathVariable UUID issueId) {
        List<AttachmentResponse> response = attachmentService.getAttachmentsByIssueId(issueId);
        return ResponseEntity.ok(response);
    }
}