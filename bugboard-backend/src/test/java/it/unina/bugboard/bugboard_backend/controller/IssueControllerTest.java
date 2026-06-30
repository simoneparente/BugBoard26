package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.IssueRequest;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.state.InProgress;
import it.unina.bugboard.bugboard_backend.service.IssueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueControllerTest {

    @Mock
    private IssueService issueService;

    @InjectMocks
    private IssueController issueController;

    private UUID issueId;
    private Issue dummyIssue;

    @BeforeEach
    void setUp() {
        issueId = UUID.randomUUID();
        dummyIssue = Issue.builder()
                .id(issueId)
                .title("NullPointer in Auth")
                .description("Description of the graphic bug")
                .status(new InProgress())
                .build();
    }

    @Test
    void createIssue_ReturnsCreatedResponse() {
        IssueRequest request = new IssueRequest();
        request.setTitle("NullPointer in Auth");
        request.setDescription("Description of the graphic bug");
        request.setProjectId(UUID.randomUUID());
        request.setCreatorId(UUID.randomUUID());
        request.setTagIds(List.of());

        when(issueService.createIssue(anyString(), anyString(), any(UUID.class), any(UUID.class), any()))
                .thenReturn(dummyIssue);

        ResponseEntity<?> responseEntity = issueController.createIssue(request);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        verify(issueService, times(1)).createIssue(anyString(), anyString(), any(UUID.class), any(UUID.class), any());
    }

    @Test
    void getIssueById_ReturnsOkResponse() {
        dummyIssue.setStatus(new it.unina.bugboard.bugboard_backend.entity.state.InProgress());

        when(issueService.getIssueById(issueId)).thenReturn(dummyIssue);

        ResponseEntity<it.unina.bugboard.bugboard_backend.dto.IssueResponse> responseEntity = (ResponseEntity<it.unina.bugboard.bugboard_backend.dto.IssueResponse>) issueController
                .getIssueById(issueId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        it.unina.bugboard.bugboard_backend.dto.IssueResponse responseBody = responseEntity.getBody();
        assertNotNull(responseBody);

        assertNotNull(responseBody.getStatus());
        assertEquals("IN_PROGRESS", responseBody.getStatus().getName());
        assertEquals("InProgress", responseBody.getStatus().getType()); // Verifica la Reflection della classe!

        verify(issueService, times(1)).getIssueById(issueId);
    }

    @Test
    void getIssuesByProject_ReturnsListWithStatusDTO() {
        UUID projectId = UUID.randomUUID();
        dummyIssue.setStatus(new it.unina.bugboard.bugboard_backend.entity.state.Closed()); // Testiamo con Closed
        
        when(issueService.getIssuesByProjectId(projectId)).thenReturn(List.of(dummyIssue));

        ResponseEntity<List<it.unina.bugboard.bugboard_backend.dto.IssueResponse>> responseEntity = 
                (ResponseEntity<List<it.unina.bugboard.bugboard_backend.dto.IssueResponse>>) issueController.getIssuesByProject(projectId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        
        List<it.unina.bugboard.bugboard_backend.dto.IssueResponse> body = responseEntity.getBody();
        assertNotNull(body);
        assertFalse(body.isEmpty());
        
        assertEquals("CLOSED", body.get(0).getStatus().getName());
        assertEquals("Closed", body.get(0).getStatus().getType());

        verify(issueService, times(1)).getIssuesByProjectId(projectId);
    }

    @Test
    void assignIssue_ReturnsOkResponse() {
        UUID assigneeId = UUID.randomUUID();
        when(issueService.assignIssue(issueId, assigneeId)).thenReturn(dummyIssue);

        ResponseEntity<?> responseEntity = issueController.assignIssue(issueId, assigneeId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        verify(issueService, times(1)).assignIssue(issueId, assigneeId);
    }

    @Test
    void startProgress_ReturnsOkResponse() {
        when(issueService.startIssueProgress(issueId)).thenReturn(dummyIssue);

        ResponseEntity<?> responseEntity = issueController.startProgress(issueId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        verify(issueService, times(1)).startIssueProgress(issueId);
    }

    @Test
    void acceptIssue_ReturnsOkResponse() {
        when(issueService.acceptIssue(issueId)).thenReturn(dummyIssue);

        ResponseEntity<?> responseEntity = issueController.acceptIssue(issueId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        verify(issueService, times(1)).acceptIssue(issueId);
    }

    @Test
    void goToPreviousState_ReturnsOkResponse() {
        when(issueService.rollbackIssueState(issueId)).thenReturn(dummyIssue);

        ResponseEntity<?> responseEntity = issueController.goToPreviousState(issueId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        verify(issueService, times(1)).rollbackIssueState(issueId);
    }

    @Test
    void removeAssignee_ReturnsOkResponse() {
        Issue issueWithoutAssignee = Issue.builder().id(issueId).assignee(null).build();
        when(issueService.removeIssueAssignee(issueId)).thenReturn(issueWithoutAssignee);

        ResponseEntity<?> responseEntity = issueController.removeAssignee(issueId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        verify(issueService, times(1)).removeIssueAssignee(issueId);
    }
}