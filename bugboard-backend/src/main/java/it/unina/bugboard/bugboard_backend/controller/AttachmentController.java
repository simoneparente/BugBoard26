package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.AttachmentResponse;
import it.unina.bugboard.bugboard_backend.dto.SasTokenResponse;
import it.unina.bugboard.bugboard_backend.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import it.unina.bugboard.bugboard_backend.service.AzureStorageService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final AzureStorageService azureStorageService;

    @PostMapping("/generate-upload-url")
    public ResponseEntity<SasTokenResponse> getUploadUrl(@RequestParam String fileName) {
        SasTokenResponse response = azureStorageService.generateUploadSasUrl(fileName);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to upload a file attached to an Issue.
     * POST /api/attachments/issue/{issueId}
     */
    @PostMapping(value = "/issue/{issueId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable UUID issueId,
            @RequestParam("file") MultipartFile file) {
        
        AttachmentResponse response = attachmentService.uploadAttachment(issueId, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint to retrieve the metadata of a single attachment.
     * GET /api/attachments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AttachmentResponse> getAttachmentById(@PathVariable UUID id) {
        AttachmentResponse response = attachmentService.getAttachmentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint to download a file attachment.
     * GET /api/attachments/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadAttachment(@PathVariable UUID id) {
        AttachmentResponse metadata = attachmentService.getAttachmentById(id);
        org.springframework.core.io.Resource resource = attachmentService.loadFileAsResource(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.fileName() + "\"")
                .body(resource);
    }

    /**
     * Endpoint to inline view a file attachment.
     * GET /api/attachments/{id}/view
     */
    @GetMapping("/{id}/view")
    public ResponseEntity<org.springframework.core.io.Resource> viewAttachment(@PathVariable UUID id) {
        AttachmentResponse metadata = attachmentService.getAttachmentById(id);
        org.springframework.core.io.Resource resource = attachmentService.loadFileAsResource(id);

        String mimeType = "application/octet-stream";
        if (metadata.fileName() != null) {
            String lower = metadata.fileName().toLowerCase();
            if (lower.endsWith(".png")) mimeType = "image/png";
            else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) mimeType = "image/jpeg";
            else if (lower.endsWith(".gif")) mimeType = "image/gif";
            else if (lower.endsWith(".svg")) mimeType = "image/svg+xml";
            else if (lower.endsWith(".pdf")) mimeType = "application/pdf";
            else if (lower.endsWith(".txt")) mimeType = "text/plain";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + metadata.fileName() + "\"")
                .body(resource);
    }

    /**
     * Endpoint to retrieve all attachments belonging to a specific Issue.
     * GET /api/attachments/issue/{issueId}
     */
    @GetMapping("/issue/{issueId}")
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsByIssueId(@PathVariable UUID issueId) {
        List<AttachmentResponse> response = attachmentService.getAttachmentsByIssueId(issueId);
        return ResponseEntity.ok(response);
    }
}