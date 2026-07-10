package it.unina.bugboard.bugboard_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unina.bugboard.bugboard_backend.dto.IssueRequest;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.IssuePriority;
import it.unina.bugboard.bugboard_backend.entity.IssueStatus;
import it.unina.bugboard.bugboard_backend.entity.IssueType;
import it.unina.bugboard.bugboard_backend.service.IssueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IssueController.class)
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

   @MockitoBean private IssueService issueService;
    private UUID projectId;
    private IssueRequest validRequest;
    private Issue dummyIssue;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();

        // Setup valid request
        validRequest = new IssueRequest();
        validRequest.setTitle("NullPointerException in Login");
        validRequest.setDescription("The server crashes if it receives blank credentials.");
        validRequest.setType(IssueType.BUG);
        validRequest.setPriority(IssuePriority.MEDIUM);

        // Mock Entity returned by the service
        dummyIssue = Issue.builder()
                .id(UUID.randomUUID())
                .title(validRequest.getTitle())
                .description(validRequest.getDescription())
                .status(IssueStatus.TO_DO)
                .priority(IssuePriority.MEDIUM)
                .type(IssueType.BUG)
                .tags(List.of())
                .attachments(List.of())
                .build();
    }

    // ==========================================
    // TC-CTRL-01: Successfull Creation of an Issue (201)
    // ==========================================
    @Test
    void createIssue_Success_ReturnsStatus201AndJson() throws Exception {
        when(issueService.createIssue(eq(projectId), any(IssueRequest.class))).thenReturn(dummyIssue);

        mockMvc.perform(post("/api/projects/{projectId}/issues", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(dummyIssue.getId().toString()))
                .andExpect(jsonPath("$.title").value("NullPointerException in Login"))
                .andExpect(jsonPath("$.status").value("TO_DO")); // Spring userà il vero mapToResponseDTO del controller

        verify(issueService, times(1)).createIssue(eq(projectId), any(IssueRequest.class));
    }

    // ==========================================
    // TC-CTRL-02: Validation Failure (Blank Title) (400)
    // ==========================================
    @Test
    void createIssue_BlankTitle_ReturnsStatus400BadRequest() throws Exception {
        validRequest.setTitle(""); 

        mockMvc.perform(post("/api/projects/{projectId}/issues", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(issueService, never()).createIssue(any(), any());
    }

    // ==========================================
    // TC-CTRL-04: Validation Failure (Blank Description) (400)
    // ==========================================
    @Test
    void createIssue_BlankDescription_ReturnsStatus400BadRequest() throws Exception {
        validRequest.setDescription(""); 

        mockMvc.perform(post("/api/projects/{projectId}/issues", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(issueService, never()).createIssue(any(), any());
    }

    // ==========================================
    // TC-CTRL-03: Project Not Found (400)
    // ==========================================
    @Test
    void createIssue_ProjectNotFound_ReturnsStatus400() throws Exception {
        when(issueService.createIssue(eq(projectId), any(IssueRequest.class)))
                .thenThrow(new IllegalArgumentException("Project not found with ID: " + projectId));

        mockMvc.perform(post("/api/projects/{projectId}/issues", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(issueService, times(1)).createIssue(eq(projectId), any(IssueRequest.class));
    }

    // ==========================================
    // TC-CTRL-05: Service Layer Exception (500)
    // ==========================================
    @Test
    void createIssue_InternalServerError_ReturnsStatus500() throws Exception {
        when(issueService.createIssue(eq(projectId), any(IssueRequest.class)))
                .thenThrow(new RuntimeException("Database Connection Timeout Exception"));

        mockMvc.perform(post("/api/projects/{projectId}/issues", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());

        verify(issueService, times(1)).createIssue(eq(projectId), any(IssueRequest.class));
    }
}