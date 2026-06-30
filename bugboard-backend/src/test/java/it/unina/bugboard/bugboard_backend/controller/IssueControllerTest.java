package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.IssueRequest;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.IssuePriority;
import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.entity.IssueType;
import it.unina.bugboard.bugboard_backend.entity.Tag;
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
        request.setTagIds(List.of());
        request.setType(IssueType.BUG);
        request.setPriority(IssuePriority.HIGH);

        when(issueService.createIssue(any(it.unina.bugboard.bugboard_backend.dto.IssueRequest.class)))
                .thenReturn(dummyIssue);

        ResponseEntity<?> responseEntity = issueController.createIssue(request);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        verify(issueService, times(1)).createIssue(any(IssueRequest.class));
    }

    @Test
    void getIssueById_ReturnsOkResponse() {
        dummyIssue.setStatus(new it.unina.bugboard.bugboard_backend.entity.state.InProgress());

        when(issueService.getIssueById(issueId)).thenReturn(dummyIssue);

        ResponseEntity<IssueResponse> responseEntity = (ResponseEntity<IssueResponse>) issueController
                .getIssueById(issueId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        IssueResponse responseBody = responseEntity.getBody();
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

        ResponseEntity<List<IssueResponse>> responseEntity = (ResponseEntity<List<IssueResponse>>) issueController
                .getIssuesByProject(projectId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        List<IssueResponse> body = responseEntity.getBody();
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

    @Test
    void getIssueById_NullTags_ReturnsEmptyTagList() {
        Issue issueWithNullTags = Issue.builder()
                .id(issueId)
                .title("No tags issue")
                .status(new InProgress())
                .tags(null)
                .build();

        when(issueService.getIssueById(issueId)).thenReturn(issueWithNullTags);

        ResponseEntity<IssueResponse> response =
                (ResponseEntity<IssueResponse>) issueController.getIssueById(issueId);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTags());
        assertTrue(response.getBody().getTags().isEmpty());
    }

    @Test
    void getIssueById_EmptyTags_ReturnsEmptyTagList() {
        Issue issueWithEmptyTags = Issue.builder()
                .id(issueId)
                .title("Empty tags issue")
                .status(new InProgress())
                .tags(List.of())
                .build();

        when(issueService.getIssueById(issueId)).thenReturn(issueWithEmptyTags);

        ResponseEntity<IssueResponse> response =
                (ResponseEntity<IssueResponse>) issueController.getIssueById(issueId);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().getTags().isEmpty());
    }

    @Test
    void getIssueById_WithTags_MapsTagFieldsCorrectly() {
        UUID tagId1 = UUID.randomUUID();
        UUID tagId2 = UUID.randomUUID();

        Tag tag1 = Tag.builder().id(tagId1).name("backend").color("#FF0000").build();
        Tag tag2 = Tag.builder().id(tagId2).name("urgent").color("#00FF00").build();

        Issue issueWithTags = Issue.builder()
                .id(issueId)
                .title("Issue with tags")
                .status(new InProgress())
                .tags(List.of(tag1, tag2))
                .build();

        when(issueService.getIssueById(issueId)).thenReturn(issueWithTags);

        ResponseEntity<IssueResponse> response =
                (ResponseEntity<IssueResponse>) issueController.getIssueById(issueId);

        assertNotNull(response.getBody());
        List<it.unina.bugboard.bugboard_backend.dto.TagResponse> tags = response.getBody().getTags();
        assertEquals(2, tags.size());

        assertEquals(tagId1, tags.get(0).getId());
        assertEquals("backend", tags.get(0).getName());
        assertEquals("#FF0000", tags.get(0).getColor());

        assertEquals(tagId2, tags.get(1).getId());
        assertEquals("urgent", tags.get(1).getName());
        assertEquals("#00FF00", tags.get(1).getColor());
    }
}