package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.AttachmentResponse;
import it.unina.bugboard.bugboard_backend.entity.Attachment;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.exception.FileStorageException;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.AttachmentRepository;
import it.unina.bugboard.bugboard_backend.repository.IssueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private IssueRepository issueRepository;

    @InjectMocks
    private AttachmentService attachmentService;

    private Issue dummyIssue;
    private UUID issueId;

    // JUnit 5 will automatically create this directory before tests and delete it afterward
    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setUp() {
        issueId = UUID.randomUUID();
        dummyIssue = Issue.builder()
                .id(issueId)
                .title("Test Issue")
                .build();

        // 1. Inject the temporary directory path into the service via reflection
        ReflectionTestUtils.setField(attachmentService, "uploadDir", tempUploadDir.toString());
        
        // 2. Manually trigger the initialization method to simulate Spring's @PostConstruct
        attachmentService.init();
    }

    @Test
    void uploadAttachment_ShouldThrowException_WhenFileIsEmpty() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile("file", "test.txt", "text/plain", new byte[0]);
        
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(dummyIssue));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            attachmentService.uploadAttachment(issueId, emptyFile);
        });

        assertEquals("Cannot upload an empty file", exception.getMessage());
        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void uploadAttachment_ShouldThrowResourceNotFound_WhenIssueDoesNotExist() {
        // Arrange
        MultipartFile validFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test Content".getBytes());
        when(issueRepository.findById(issueId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            attachmentService.uploadAttachment(issueId, validFile);
        });

        assertEquals(String.format("Issue with id %s not found", issueId), exception.getMessage());
        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void uploadAttachment_ShouldSaveAndReturnAttachmentResponse_WhenValid() {
        // Arrange
        MultipartFile validFile = new MockMultipartFile("file", "test.png", "image/png", "Fake Image Content".getBytes());
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(dummyIssue));
        
        Attachment savedAttachment = new Attachment();
        savedAttachment.setId(UUID.randomUUID());
        savedAttachment.setFileName("test.png");
        savedAttachment.setIssue(dummyIssue);

        when(attachmentRepository.save(any(Attachment.class))).thenReturn(savedAttachment);

        // Act
        AttachmentResponse result = attachmentService.uploadAttachment(issueId, validFile);

        // Assert
        assertNotNull(result);
        assertEquals("test.png", result.fileName()); 
        
        verify(issueRepository, times(1)).findById(issueId);
        verify(attachmentRepository, times(1)).save(any(Attachment.class));
    }

    @Test
    void getAttachmentById_ShouldReturnAttachmentResponse_WhenFound() {
        // Arrange
        UUID attachmentId = UUID.randomUUID();
        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        attachment.setFileName("document.pdf");
        attachment.setIssue(dummyIssue);

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        // Act
        AttachmentResponse result = attachmentService.getAttachmentById(attachmentId);

        // Assert
        assertNotNull(result);
        assertEquals("document.pdf", result.fileName());
        verify(attachmentRepository, times(1)).findById(attachmentId);
    }

    @Test
    void getAttachmentById_ShouldThrowResourceNotFound_WhenNotFound() {
        // Arrange
        UUID attachmentId = UUID.randomUUID();
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            attachmentService.getAttachmentById(attachmentId);
        });

        assertEquals(String.format("Attachment with id %s not found", attachmentId), exception.getMessage());
    }

    @Test
    void getAttachmentsByIssueId_ShouldReturnListOfAttachmentResponses() {
        // Arrange
        Attachment attachment = new Attachment();
        attachment.setId(UUID.randomUUID());
        attachment.setFileName("screen.png");
        attachment.setIssue(dummyIssue);

        when(attachmentRepository.findByIssueId(issueId)).thenReturn(List.of(attachment));

        // Act
        List<AttachmentResponse> result = attachmentService.getAttachmentsByIssueId(issueId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("screen.png", result.get(0).fileName());
        verify(attachmentRepository, times(1)).findByIssueId(issueId);
    }

    @Test
    void uploadAttachment_ShouldHandleFilesWithoutExtension() {
        // Arrange
        MultipartFile fileWithoutExt = new MockMultipartFile("file", "testfile", "text/plain", "Content".getBytes());
        when(issueRepository.findById(issueId)).thenReturn(Optional.of(dummyIssue));
        
        Attachment savedAttachment = new Attachment();
        savedAttachment.setId(UUID.randomUUID());
        savedAttachment.setFileName("testfile");
        savedAttachment.setFileExtension("");
        savedAttachment.setIssue(dummyIssue);

        when(attachmentRepository.save(any(Attachment.class))).thenReturn(savedAttachment);

        // Act
        AttachmentResponse result = attachmentService.uploadAttachment(issueId, fileWithoutExt);

        // Assert
        assertNotNull(result);
        assertEquals("", result.fileExtension());
        verify(attachmentRepository, times(1)).save(any(Attachment.class));
    }

    @Test
    void uploadAttachment_ShouldThrowFileStorageException_OnIOException() throws java.io.IOException {
        // Arrange
        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.isEmpty()).thenReturn(false);
        when(badFile.getOriginalFilename()).thenReturn("test.png");
        when(badFile.getInputStream()).thenThrow(new java.io.IOException("Simulated IO Error"));

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(dummyIssue));

        // Act & Assert
        FileStorageException exception = assertThrows(FileStorageException.class, () -> {
            attachmentService.uploadAttachment(issueId, badFile);
        });

        assertTrue(exception.getMessage().contains("Could not store file test.png"));
    }

    @Test
    void uploadAttachment_ShouldHandleNullOriginalFileName() throws java.io.IOException {
        // Arrange
        // We use Mockito to strictly force the return of a real 'null', bypassing MockMultipartFile logic
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn(null); // Force real null!
        when(mockFile.getSize()).thenReturn(10L);
        // We must provide a valid InputStream because Files.copy will try to read from it
        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("Content".getBytes()));

        when(issueRepository.findById(issueId)).thenReturn(Optional.of(dummyIssue));

        Attachment savedAttachment = new Attachment();
        savedAttachment.setId(UUID.randomUUID());
        savedAttachment.setFileName(null);
        savedAttachment.setFileExtension("");
        savedAttachment.setIssue(dummyIssue);

        when(attachmentRepository.save(any(Attachment.class))).thenReturn(savedAttachment);

        // Act
        AttachmentResponse result = attachmentService.uploadAttachment(issueId, mockFile);

        // Assert
        assertNotNull(result);
        assertEquals("", result.fileExtension());
        assertNull(result.fileName());
        verify(attachmentRepository, times(1)).save(any(Attachment.class));
    }
    
    @Test
    void init_ShouldThrowRuntimeException_OnIoException() throws java.io.IOException {
        // Arrange
        // Create an ACTUAL physical file (not a directory) inside the temporary folder
        Path existingFile = java.nio.file.Files.createFile(tempUploadDir.resolve("dummyFile.txt"));
        
        // Tell the AttachmentService to use this file path as the upload directory
        ReflectionTestUtils.setField(attachmentService, "uploadDir", existingFile.toString());
        
        // Act & Assert
        // Attempting to create a directory over an existing physical file will trigger an IOException
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            attachmentService.init();
        });
        
        assertEquals("Could not create upload directory!", exception.getMessage());
    }

    @Test
    void loadFileAsResource_Success() throws java.io.IOException {
        UUID attachmentId = UUID.randomUUID();
        Path testFile = tempUploadDir.resolve("test-download.txt");
        java.nio.file.Files.writeString(testFile, "Hello World");

        Attachment attachment = Attachment.builder()
                .id(attachmentId)
                .fileName("test-download.txt")
                .filePath(testFile.toString())
                .build();
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        org.springframework.core.io.Resource resource = attachmentService.loadFileAsResource(attachmentId);

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void loadFileAsResource_ThrowsWhenFileNotFound() {
        UUID attachmentId = UUID.randomUUID();
        Attachment attachment = Attachment.builder()
                .id(attachmentId)
                .fileName("not-exists.txt")
                .filePath(tempUploadDir.resolve("not-exists.txt").toString())
                .build();
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        assertThrows(ResourceNotFoundException.class, () -> attachmentService.loadFileAsResource(attachmentId));
    }

    @Test
    void loadFileAsResource_ThrowsWhenMalformedUrl() {
        UUID attachmentId = UUID.randomUUID();
        Attachment attachment = Attachment.builder()
                .id(attachmentId)
                .fileName("bad.txt")
                .filePath("\u0000://invalid")
                .build();
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        assertThrows(java.nio.file.InvalidPathException.class, () -> attachmentService.loadFileAsResource(attachmentId));
    }
}