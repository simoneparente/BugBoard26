package it.unina.bugboard.bugboard_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unina.bugboard.bugboard_backend.dto.IssueRequest;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.IssuePriority;
import it.unina.bugboard.bugboard_backend.entity.IssueStatus;
import it.unina.bugboard.bugboard_backend.entity.IssueType;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.service.IssueService;
import it.unina.bugboard.bugboard_backend.security.JwtService;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IssueController.class)
@WithMockUser
class IssueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private IssueService issueService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;
    private String projectKey;
    private Long sequenceNumber;
    private UUID issueId;
    private Project project;
    private IssueRequest validRequest;
    private Issue dummyIssue;

    @BeforeEach
    void setUp() {
        projectKey = "FRONT";
        sequenceNumber = 1L;
        project = Project.builder()
                .id(UUID.randomUUID())
                .key(projectKey)
                .name("BugBoard Core")
                .description("Test project")
                .build();

        // Setup valid request
        validRequest = new IssueRequest();
        validRequest.setTitle("NullPointerException in Login");
        validRequest.setDescription("The server crashes if it receives blank credentials.");
        validRequest.setType(IssueType.BUG);
        validRequest.setPriority(IssuePriority.MEDIUM);

        // Mock Entity returned by the service
        dummyIssue = Issue.builder()
                .id(UUID.randomUUID())
                .sequenceNumber(sequenceNumber)
                .title(validRequest.getTitle())
                .description(validRequest.getDescription())
                .project(project)
                .status(IssueStatus.TO_DO)
                .priority(IssuePriority.MEDIUM)
                .type(IssueType.BUG)
                .tags(List.of())
                .attachments(List.of())
                .build();

        issueId = dummyIssue.getId();
    }

    // ==========================================
    // TC-CTRL-01: Successfull Creation of an Issue (201)
    // ==========================================
    @Test
    void createIssue_Success_ReturnsStatus201AndJson() throws Exception {
        when(issueService.createIssue(eq(projectKey), any(IssueRequest.class))).thenReturn(dummyIssue);

        mockMvc.perform(post("/api/projects/{projectKey}/issues", projectKey)
                .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(dummyIssue.getId().toString()))
                .andExpect(jsonPath("$.title").value("NullPointerException in Login"))
                .andExpect(jsonPath("$.status").value("TO_DO")); // Spring will use the real mapToResponseDTO of the controller

        verify(issueService, times(1)).createIssue(eq(projectKey), any(IssueRequest.class));
    }

    // ==========================================
    // TC-CTRL-02: Validation Failure (Blank Title) (400)
    // ==========================================
    @Test
    void createIssue_BlankTitle_ReturnsStatus400BadRequest() throws Exception {
        validRequest.setTitle(""); 

        mockMvc.perform(post("/api/projects/{projectKey}/issues", projectKey)
                .with(csrf())
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

        mockMvc.perform(post("/api/projects/{projectKey}/issues", projectKey)
                .with(csrf())
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
        when(issueService.createIssue(eq(projectKey), any(IssueRequest.class)))
                .thenThrow(new IllegalArgumentException("Project not found with key: " + projectKey));

        mockMvc.perform(post("/api/projects/{projectKey}/issues", projectKey)
                .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(issueService, times(1)).createIssue(eq(projectKey), any(IssueRequest.class));
    }

    // ==========================================
    // TC-CTRL-05: Service Layer Exception (500)
    // ==========================================
    @Test
    void createIssue_InternalServerError_ReturnsStatus500() throws Exception {
        when(issueService.createIssue(eq(projectKey), any(IssueRequest.class)))
                .thenThrow(new RuntimeException("Database Connection Timeout Exception"));

        mockMvc.perform(post("/api/projects/{projectKey}/issues", projectKey)
                .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());

        verify(issueService, times(1)).createIssue(eq(projectKey), any(IssueRequest.class));
    }

    @Test
    void setStatus_Success_ReturnsStatus200() throws Exception {
        UUID dummyIssueId = dummyIssue.getId();
        dummyIssue.setStatus(IssueStatus.IN_PROGRESS);
        when(issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber)).thenReturn(dummyIssue);
        when(issueService.setStatus(eq(dummyIssueId), eq(IssueStatus.IN_PROGRESS))).thenReturn(dummyIssue);

        mockMvc.perform(put("/api/projects/{projectKey}/issues/{sequenceNumber}/status", projectKey, sequenceNumber)
                .with(csrf())
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(issueService, times(1)).setStatus(eq(dummyIssueId), eq(IssueStatus.IN_PROGRESS));
    }

    @Test
    void getIssuesByProjectKey_Success_ReturnsStatus200AndPagedJson() throws Exception {
        Page<Issue> pagedResult = new PageImpl<>(List.of(dummyIssue));

        when(issueService.getIssuesByProjectKey(eq(projectKey), eq("ALL"), eq("ALL"), eq("ALL"), eq(null), any(Pageable.class)))
                .thenReturn(pagedResult);

        mockMvc.perform(get("/api/projects/{projectKey}/issues", projectKey)
                .param("status", "ALL")
                .param("priority", "ALL")
                .param("type", "ALL")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "title,asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(dummyIssue.getId().toString()))
                .andExpect(jsonPath("$.content[0].title").value("NullPointerException in Login"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(issueService, times(1)).getIssuesByProjectKey(eq(projectKey), eq("ALL"), eq("ALL"), eq("ALL"), eq(null), any(Pageable.class));
    }

    @Test
    void getIssuesByProjectKey_WithSearchParam_Success() throws Exception {
        Page<Issue> pagedResult = new PageImpl<>(List.of(dummyIssue));

        when(issueService.getIssuesByProjectKey(eq(projectKey), eq("ALL"), eq("ALL"), eq("ALL"), eq("login"), any(Pageable.class)))
                .thenReturn(pagedResult);

        mockMvc.perform(get("/api/projects/{projectKey}/issues", projectKey)
                .param("status", "ALL")
                .param("priority", "ALL")
                .param("type", "ALL")
                .param("search", "login")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "title,asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(dummyIssue.getId().toString()))
                .andExpect(jsonPath("$.content[0].title").value("NullPointerException in Login"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(issueService, times(1)).getIssuesByProjectKey(eq(projectKey), eq("ALL"), eq("ALL"), eq("ALL"), eq("login"), any(Pageable.class));
    }

    @Test
    void getIssuesByProjectKey_WithTypeParam_Success() throws Exception {
        Page<Issue> pagedResult = new PageImpl<>(List.of(dummyIssue));

        when(issueService.getIssuesByProjectKey(eq(projectKey), eq("ALL"), eq("ALL"), eq("FEATURE"), eq(null), any(Pageable.class)))
                .thenReturn(pagedResult);

        mockMvc.perform(get("/api/projects/{projectKey}/issues", projectKey)
                .param("status", "ALL")
                .param("priority", "ALL")
                .param("type", "FEATURE")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "title,asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(issueService, times(1)).getIssuesByProjectKey(eq(projectKey), eq("ALL"), eq("ALL"), eq("FEATURE"), eq(null), any(Pageable.class));
    }

    @Test
    void getIssueByProjectKeyAndSequenceNumber_Success_ReturnsStatus200AndIssue() throws Exception {
        when(issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber)).thenReturn(dummyIssue);

        mockMvc.perform(get("/api/projects/{projectKey}/issues/{sequenceNumber}", projectKey, sequenceNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(issueId.toString()))
                .andExpect(jsonPath("$.title").value("NullPointerException in Login"));

        verify(issueService, times(1)).getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
    }

    @Test
    void getIssueByProjectKeyAndSequenceNumber_NotFound_ReturnsStatus404() throws Exception {
        when(issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber))
                .thenThrow(new ResourceNotFoundException("Issue not found"));

        mockMvc.perform(get("/api/projects/{projectKey}/issues/{sequenceNumber}", projectKey, sequenceNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(issueService, times(1)).getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
    }

    @Test
    void deleteIssue_Success_ReturnsStatus204NoContent() throws Exception {
        when(issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber)).thenReturn(dummyIssue);
        doNothing().when(issueService).deleteIssue(issueId);

        mockMvc.perform(delete("/api/projects/{projectKey}/issues/{sequenceNumber}", projectKey, sequenceNumber)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(issueService, times(1)).deleteIssue(issueId);
    }
}
