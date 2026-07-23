package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.config.SecurityConfig;
import it.unina.bugboard.bugboard_backend.dto.AttachmentResponse;
import it.unina.bugboard.bugboard_backend.dto.SasTokenResponse;
import it.unina.bugboard.bugboard_backend.service.AttachmentService;
import it.unina.bugboard.bugboard_backend.service.AzureStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import it.unina.bugboard.bugboard_backend.security.JwtAuthenticationFilter;

@WebMvcTest(controllers = AttachmentController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration.class
}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { SecurityConfig.class,
        JwtAuthenticationFilter.class }))
@AutoConfigureMockMvc(addFilters = false)
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttachmentService attachmentService;

    @MockitoBean
        private AzureStorageService azureStorageService;

    private UUID issueId;
    private UUID attachmentId;
    private AttachmentResponse dummyResponse;

    private static final String TEST_FILE_NAME = "test.txt";

    @BeforeEach
    void setUp() {
        issueId = UUID.randomUUID();
        attachmentId = UUID.randomUUID();
        dummyResponse = AttachmentResponse.builder()
                .id(attachmentId)
                .fileName(TEST_FILE_NAME)
                .filePath("uploads/" + attachmentId + ".txt")
                .fileSize(100L)
                .fileExtension(".txt")
                .issueId(issueId)
                .build();
    }

    @Test
    void uploadAttachment_ReturnsCreatedResponse() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", TEST_FILE_NAME, "text/plain", "Hello".getBytes());
        when(attachmentService.uploadAttachment(eq(issueId), any())).thenReturn(dummyResponse);

        mockMvc.perform(multipart("/api/attachments/issue/{issueId}", issueId)
                .file(mockFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(attachmentId.toString()))
                .andExpect(jsonPath("$.fileName").value(TEST_FILE_NAME))
                .andExpect(jsonPath("$.fileSize").value(100))
                .andExpect(jsonPath("$.fileExtension").value(".txt"))
                .andExpect(jsonPath("$.issueId").value(issueId.toString()));

        verify(attachmentService, times(1)).uploadAttachment(eq(issueId), any());
    }

        @Test
        void getUploadUrl_ReturnsOkResponse() throws Exception {
                SasTokenResponse response = new SasTokenResponse("https://example.blob.core.windows.net/attachments/test.txt?sas", "test.txt");
                when(azureStorageService.generateUploadSasUrl("test.txt")).thenReturn(response);

                mockMvc.perform(post("/api/attachments/generate-upload-url")
                                                .param("fileName", "test.txt"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.uploadUrl").value(response.getUploadUrl()))
                                .andExpect(jsonPath("$.blobFileName").value(response.getBlobFileName()));

                verify(azureStorageService, times(1)).generateUploadSasUrl("test.txt");
        }

    @Test
    void getAttachmentById_ReturnsOkResponse() throws Exception {
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(dummyResponse);

        mockMvc.perform(get("/api/attachments/{id}", attachmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attachmentId.toString()))
                .andExpect(jsonPath("$.fileName").value(TEST_FILE_NAME));

        verify(attachmentService, times(1)).getAttachmentById(attachmentId);
    }

    @Test
    void getAttachmentsByIssueId_ReturnsOkResponse() throws Exception {
        List<AttachmentResponse> responseList = List.of(dummyResponse);
        when(attachmentService.getAttachmentsByIssueId(issueId)).thenReturn(responseList);

        mockMvc.perform(get("/api/attachments/issue/{issueId}", issueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(attachmentId.toString()))
                .andExpect(jsonPath("$[0].fileName").value(TEST_FILE_NAME));

        verify(attachmentService, times(1)).getAttachmentsByIssueId(issueId);
    }

    @Test
    void downloadAttachment_ReturnsResourceWithAttachmentHeader() throws Exception {
        org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource("content".getBytes());
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(dummyResponse);
        when(attachmentService.loadFileAsResource(attachmentId)).thenReturn(resource);

        mockMvc.perform(get("/api/attachments/{id}/download", attachmentId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + TEST_FILE_NAME + "\""));
    }

    @Test
    void viewAttachment_ReturnsResourceWithInlineHeader() throws Exception {
        org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource("content".getBytes());
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(dummyResponse);
        when(attachmentService.loadFileAsResource(attachmentId)).thenReturn(resource);

        mockMvc.perform(get("/api/attachments/{id}/view", attachmentId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "inline; filename=\"" + TEST_FILE_NAME + "\""))
                .andExpect(header().string("Content-Type", "text/plain"));
    }

    @Test
    void viewAttachment_DetectsVariousMimeTypes() throws Exception {
        org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource("content".getBytes());

        String[] filenames = {"image.png", "photo.jpg", "photo.jpeg", "anim.gif", "vector.svg", "doc.pdf", "unknown.bin"};
        String[] expectedMime = {"image/png", "image/jpeg", "image/jpeg", "image/gif", "image/svg+xml", "application/pdf", "application/octet-stream"};

        for (int i = 0; i < filenames.length; i++) {
            UUID id = UUID.randomUUID();
            AttachmentResponse response = AttachmentResponse.builder().id(id).fileName(filenames[i]).filePath("uploads/" + filenames[i]).build();
            when(attachmentService.getAttachmentById(id)).thenReturn(response);
            when(attachmentService.loadFileAsResource(id)).thenReturn(resource);

            mockMvc.perform(get("/api/attachments/{id}/view", id))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", expectedMime[i]));
        }
    }

    @Test
    void viewAttachment_HandlesNullFileName() throws Exception {
        org.springframework.core.io.Resource resource = new org.springframework.core.io.ByteArrayResource("content".getBytes());
        UUID id = UUID.randomUUID();
        AttachmentResponse response = AttachmentResponse.builder().id(id).fileName(null).filePath("uploads/nullfile").build();
        when(attachmentService.getAttachmentById(id)).thenReturn(response);
        when(attachmentService.loadFileAsResource(id)).thenReturn(resource);

        mockMvc.perform(get("/api/attachments/{id}/view", id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"));
    }

    @Test
    void getUploadUrl_HandlesNullResponse() throws Exception {
        when(azureStorageService.generateUploadSasUrl("test.txt")).thenReturn(null);

        mockMvc.perform(post("/api/attachments/generate-upload-url")
                        .param("fileName", "test.txt"))
                .andExpect(status().isOk());
    }

    @Test
    void getUploadUrl_HandlesNullUploadUrl() throws Exception {
        SasTokenResponse response = new SasTokenResponse(null, "test.txt");
        when(azureStorageService.generateUploadSasUrl("test.txt")).thenReturn(response);

        mockMvc.perform(post("/api/attachments/generate-upload-url")
                        .param("fileName", "test.txt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blobFileName").value("test.txt"));
    }

    @Test
    void downloadAttachment_HandlesNullSasUrl_WhenLocalFileMissing() throws Exception {
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(dummyResponse);
        when(attachmentService.loadFileAsResource(attachmentId)).thenThrow(new it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException("File not found on disk"));
        when(azureStorageService.generateDownloadSasUrl(dummyResponse.filePath())).thenReturn(null);

        mockMvc.perform(get("/api/attachments/{id}/download", attachmentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void viewAttachment_HandlesNullSasUrl_WhenLocalFileMissing() throws Exception {
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(dummyResponse);
        when(attachmentService.loadFileAsResource(attachmentId)).thenThrow(new it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException("File not found on disk"));
        when(azureStorageService.generateDownloadSasUrl(dummyResponse.filePath())).thenReturn(null);

        mockMvc.perform(get("/api/attachments/{id}/view", attachmentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUploadUrl_MapsDockerInternalAzuriteUrlToHostPort() throws Exception {
        SasTokenResponse response = new SasTokenResponse("http://azurite:10000/attachments/test.txt?sas", "test.txt");
        when(azureStorageService.generateUploadSasUrl("test.txt")).thenReturn(response);

        mockMvc.perform(post("/api/attachments/generate-upload-url")
                        .param("fileName", "test.txt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("http://127.0.0.1:10001/attachments/test.txt?sas"))
                .andExpect(jsonPath("$.blobFileName").value("test.txt"));
    }

    @Test
    void downloadAttachment_RedirectsToAzureSasUrl_WhenLocalFileMissing() throws Exception {
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(dummyResponse);
        when(attachmentService.loadFileAsResource(attachmentId)).thenThrow(new it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException("File not found on disk"));
        when(azureStorageService.generateDownloadSasUrl(dummyResponse.filePath())).thenReturn("http://azurite:10000/attachments/test.txt?sas");

        mockMvc.perform(get("/api/attachments/{id}/download", attachmentId))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://127.0.0.1:10001/attachments/test.txt?sas"));
    }

    @Test
    void viewAttachment_RedirectsToAzureSasUrl_WhenLocalFileMissing() throws Exception {
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(dummyResponse);
        when(attachmentService.loadFileAsResource(attachmentId)).thenThrow(new it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException("File not found on disk"));
        when(azureStorageService.generateDownloadSasUrl(dummyResponse.filePath())).thenReturn("https://bugboard26.blob.core.windows.net/attachments/test.txt?sas");

        mockMvc.perform(get("/api/attachments/{id}/view", attachmentId))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://bugboard26.blob.core.windows.net/attachments/test.txt?sas"));
    }

    @Test
    void getUploadUrl_LeavesRealAzureUrlIntact() throws Exception {
        SasTokenResponse response = new SasTokenResponse("https://bugboard26.blob.core.windows.net/attachments/test.txt?sas", "test.txt");
        when(azureStorageService.generateUploadSasUrl("test.txt")).thenReturn(response);

        mockMvc.perform(post("/api/attachments/generate-upload-url")
                        .param("fileName", "test.txt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("https://bugboard26.blob.core.windows.net/attachments/test.txt?sas"))
                .andExpect(jsonPath("$.blobFileName").value("test.txt"));
    }

    @Test
    void viewAttachment_RedirectsToAzuriteHostPort_WhenLocalFileMissing() throws Exception {
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(dummyResponse);
        when(attachmentService.loadFileAsResource(attachmentId)).thenThrow(new it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException("File not found on disk"));
        when(azureStorageService.generateDownloadSasUrl(dummyResponse.filePath())).thenReturn("http://azurite:10000/attachments/test.txt?sas");

        mockMvc.perform(get("/api/attachments/{id}/view", attachmentId))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://127.0.0.1:10001/attachments/test.txt?sas"));
    }

    @Test
    void downloadAttachment_LeavesRealAzureUrlIntact_WhenLocalFileMissing() throws Exception {
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(dummyResponse);
        when(attachmentService.loadFileAsResource(attachmentId)).thenThrow(new it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException("File not found on disk"));
        when(azureStorageService.generateDownloadSasUrl(dummyResponse.filePath())).thenReturn("https://bugboard26.blob.core.windows.net/attachments/test.txt?sas");

        mockMvc.perform(get("/api/attachments/{id}/download", attachmentId))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://bugboard26.blob.core.windows.net/attachments/test.txt?sas"));
    }
}
