package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.AttachmentResponse;
import it.unina.bugboard.bugboard_backend.entity.Attachment;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.AttachmentRepository;
import it.unina.bugboard.bugboard_backend.repository.IssueRepository;
import it.unina.bugboard.bugboard_backend.exception.FileStorageException;
import it.unina.bugboard.bugboard_backend.exception.UploadDirectoryException;
import jakarta.annotation.PostConstruct;
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
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final IssueRepository issueRepository;
    private final String uploadDir;

    public AttachmentService(
            AttachmentRepository attachmentRepository,
            IssueRepository issueRepository,
            @Value("${file.upload-dir:uploads}") String uploadDir) {
        this.attachmentRepository = attachmentRepository;
        this.issueRepository = issueRepository;
        this.uploadDir = uploadDir;
    }

    // This method is executed automatically at Spring Boot startup
    // Ensures the upload directory exists on disk
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new UploadDirectoryException("Could not create upload directory!", e);
        }
    }

    @Transactional
    public AttachmentResponse uploadAttachment(UUID issueId, MultipartFile file) {
        Issue issue = getIssueOrThrow(issueId);
        validateFileNotEmpty(file);

        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        Path targetLocation = generateTargetLocation(extension);

        saveFileToStorage(file, targetLocation);

        Attachment savedAttachment = saveAttachmentToDatabase(
                originalFileName, targetLocation.toString(), file.getSize(), extension, issue
        );

        return mapToResponse(savedAttachment);
    }

    private Issue getIssueOrThrow(UUID issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Issue with id %s not found", issueId)));
    }

    private void validateFileNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload an empty file");
        }
    }

    private String getFileExtension(String originalFileName) {
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return "";
    }

    private Path generateTargetLocation(String extension) {
        String physicalFileName = UUID.randomUUID() + extension;
        return Paths.get(uploadDir).resolve(physicalFileName);
    }

    private void saveFileToStorage(MultipartFile file, Path targetLocation) {
        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + file.getOriginalFilename() + ". Please try again!", ex);
        }
    }

    private Attachment saveAttachmentToDatabase(String originalFileName, String filePath, long fileSize, String extension, Issue issue) {
        Attachment attachment = Attachment.builder()
                .fileName(originalFileName)
                .filePath(filePath)
                .fileSize(fileSize)
                .fileExtension(extension)
                .issue(issue)
                .build();
        return attachmentRepository.save(attachment);
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