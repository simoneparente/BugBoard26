package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.config.SecurityConfig;
import it.unina.bugboard.bugboard_backend.dto.AttachmentResponse;
import it.unina.bugboard.bugboard_backend.service.AttachmentService;
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
    private it.unina.bugboard.bugboard_backend.security.JwtService jwtService;

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
}
