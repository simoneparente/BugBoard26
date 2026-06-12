package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.AttachmentResponse;
import it.unina.bugboard.bugboard_backend.entity.Attachment;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.AttachmentRepository;
import it.unina.bugboard.bugboard_backend.repository.IssueRepository;
import it.unina.bugboard.bugboard_backend.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final IssueRepository issueRepository;

    // Get the upload directory from configuration, defaulting to "uploads"
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // This method is executed automatically at Spring Boot startup
    // Ensures the upload directory exists on disk
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!");
        }
    }

    @Transactional
    public AttachmentResponse uploadAttachment(UUID issueId, MultipartFile file) {
        // 1. Check that the Issue exists
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Issue with id %s not found", issueId)));

        // 2. Check that the file is not empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }

        try {
            // 3. Extract file metadata
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // 4. Generate a unique physical filename to avoid overwriting files with the same name
            String physicalFileName = UUID.randomUUID() + extension;
            Path targetLocation = Paths.get(uploadDir).resolve(physicalFileName);

            // 5. Physically save the file to the container
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 6. Persist metadata in the database
            Attachment attachment = Attachment.builder()
                    .fileName(originalFileName)         // Original name (e.g., "screen.png")
                    .filePath(targetLocation.toString()) // Physical path (e.g., "uploads/123-456.png")
                    .fileSize(file.getSize())           // Size in bytes
                    .fileExtension(extension)           // File extension (e.g., ".png")
                    .issue(issue)                       // Link to the Issue
                    .build();

            Attachment savedAttachment = attachmentRepository.save(attachment);
            return mapToResponse(savedAttachment);

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + file.getOriginalFilename() + ". Please try again!", ex);
        }
    }

    public List<AttachmentResponse> getAttachmentsByIssueId(UUID issueId) {
        return attachmentRepository.findByIssueId(issueId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AttachmentResponse getAttachmentById(UUID id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Attachment with id %s not found", id)));
        return mapToResponse(attachment);
    }

    private AttachmentResponse mapToResponse(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .filePath(attachment.getFilePath())
                .fileSize(attachment.getFileSize())
                .fileExtension(attachment.getFileExtension())
                .issueId(attachment.getIssue().getId())
                .build();
    }
}