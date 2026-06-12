package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.AttachmentResponse;
import it.unina.bugboard.bugboard_backend.service.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentControllerTest {

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private AttachmentController attachmentController;

    private UUID issueId;
    private UUID attachmentId;
    private AttachmentResponse dummyResponse;

    @BeforeEach
    void setUp() {
        issueId = UUID.randomUUID();
        attachmentId = UUID.randomUUID();
        dummyResponse = AttachmentResponse.builder()
                .id(attachmentId)
                .fileName("test.txt")
                .filePath("uploads/" + attachmentId + ".txt")
                .fileSize(100L)
                .fileExtension(".txt")
                .issueId(issueId)
                .build();
    }

    @Test
    void uploadAttachment_ReturnsCreatedResponse() {
        MultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
        when(attachmentService.uploadAttachment(issueId, mockFile)).thenReturn(dummyResponse);

        ResponseEntity<AttachmentResponse> responseEntity = attachmentController.uploadAttachment(issueId, mockFile);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(dummyResponse, responseEntity.getBody());
        verify(attachmentService, times(1)).uploadAttachment(issueId, mockFile);
    }

    @Test
    void getAttachmentById_ReturnsOkResponse() {
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(dummyResponse);

        ResponseEntity<AttachmentResponse> responseEntity = attachmentController.getAttachmentById(attachmentId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(dummyResponse, responseEntity.getBody());
        verify(attachmentService, times(1)).getAttachmentById(attachmentId);
    }

    @Test
    void getAttachmentsByIssueId_ReturnsOkResponse() {
        List<AttachmentResponse> responseList = List.of(dummyResponse);
        when(attachmentService.getAttachmentsByIssueId(issueId)).thenReturn(responseList);

        ResponseEntity<List<AttachmentResponse>> responseEntity = attachmentController.getAttachmentsByIssueId(issueId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(responseList, responseEntity.getBody());
        verify(attachmentService, times(1)).getAttachmentsByIssueId(issueId);
    }
}
