package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.AttachmentResponse;
import it.unina.bugboard.bugboard_backend.dto.SasTokenResponse;
import it.unina.bugboard.bugboard_backend.service.AttachmentService;
import it.unina.bugboard.bugboard_backend.service.AzureStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final AzureStorageService azureStorageService;

    private static final String AZURITE_DOCKER = "http://azurite:10000";
    private static final String AZURITE_LOCAL = "http://127.0.0.1:10001";

    @PostMapping("/generate-upload-url")
    public ResponseEntity<SasTokenResponse> getUploadUrl(@RequestParam String fileName) {
        SasTokenResponse response = azureStorageService.generateUploadSasUrl(fileName);
        if (response != null && response.getUploadUrl() != null && response.getUploadUrl().contains(AZURITE_DOCKER)) {
            String externalUrl = response.getUploadUrl().replace(AZURITE_DOCKER, AZURITE_LOCAL);
            response = new SasTokenResponse(externalUrl, response.getBlobFileName());
        }
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
    public ResponseEntity<?> downloadAttachment(@PathVariable UUID id) {
        AttachmentResponse metadata = attachmentService.getAttachmentById(id);
        try {
            org.springframework.core.io.Resource resource = attachmentService.loadFileAsResource(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.fileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            String mimeType = getMimeType(metadata.fileName());
            String sasUrl = azureStorageService.generateDownloadSasUrl(
                    metadata.filePath(),
                    "attachment; filename=\"" + metadata.fileName() + "\"",
                    mimeType
            );
            if (sasUrl == null) {
                sasUrl = azureStorageService.generateDownloadSasUrl(metadata.filePath());
            }
            if (sasUrl != null && sasUrl.contains(AZURITE_DOCKER)) {
                sasUrl = sasUrl.replace(AZURITE_DOCKER, AZURITE_LOCAL);
            }
            if (sasUrl == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(sasUrl)).build();
        }
    }

    /**
     * Endpoint to inline view a file attachment.
     * GET /api/attachments/{id}/view
     */
    @GetMapping("/{id}/view")
    public ResponseEntity<?> viewAttachment(@PathVariable UUID id) {
        AttachmentResponse metadata = attachmentService.getAttachmentById(id);
        try {
            org.springframework.core.io.Resource resource = attachmentService.loadFileAsResource(id);
            String mimeType = getMimeType(metadata.fileName());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + metadata.fileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            String mimeType = getMimeType(metadata.fileName());
            String sasUrl = azureStorageService.generateDownloadSasUrl(
                    metadata.filePath(),
                    "inline; filename=\"" + metadata.fileName() + "\"",
                    mimeType
            );
            if (sasUrl == null) {
                sasUrl = azureStorageService.generateDownloadSasUrl(metadata.filePath());
            }
            if (sasUrl != null && sasUrl.contains(AZURITE_DOCKER)) {
                sasUrl = sasUrl.replace(AZURITE_DOCKER, AZURITE_LOCAL);
            }
            if (sasUrl == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(sasUrl)).build();
        }
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

    private String getMimeType(String fileName) {
        if (fileName == null) return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return MediaTypeFactory.getMediaType(fileName)
                .map(MediaType::toString)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }
}