package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.AttachmentMetadataRequest;
import it.unina.bugboard.bugboard_backend.dto.IssueRequest;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.entity.Attachment;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.IssuePriority;
import it.unina.bugboard.bugboard_backend.entity.IssueStatus;
import it.unina.bugboard.bugboard_backend.entity.IssueType;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Tag;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.AttachmentRepository;
import it.unina.bugboard.bugboard_backend.repository.IssueRepository;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import it.unina.bugboard.bugboard_backend.repository.TagRepository;
import it.unina.bugboard.bugboard_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

	@Mock
	private IssueRepository issueRepository;

	@Mock
	private ProjectRepository projectRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TagRepository tagRepository;

	@Mock
	private AttachmentRepository attachmentRepository;

	@Mock
	private ProjectService projectService;

	@InjectMocks
	private IssueService issueService;

	private UUID projectId;
	private Project project;

	@BeforeEach
	void setUp() {
		projectId = UUID.randomUUID();
		project = Project.builder()
				.id(projectId)
				.name("BugBoard Core")
				.description("test project")
				.build();
	}

	@Test
	void createIssue_UsesDefaultStatusAndNoAssignee_WhenStatusAndTagsAreMissing() {
		IssueRequest request = createRequest(null, null, null);
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectId, request);

		assertNotNull(result);
		assertEquals("Issue title", result.getTitle());
		assertEquals("Issue description", result.getDescription());
		assertEquals(project, result.getProject());
		assertEquals(IssueStatus.TO_DO, result.getStatus());
		assertEquals(IssuePriority.MEDIUM, result.getPriority());
		assertEquals(IssueType.BUG, result.getType());
		assertNull(result.getAssignee());
		assertNotNull(result.getTags());
		assertEquals(0, result.getTags().size());
		verify(issueRepository).save(any(Issue.class));
	}

	@Test
	void createIssue_PreservesExplicitStatus() {
		IssueRequest request = createRequest(IssueStatus.IN_PROGRESS, null, null);
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectId, request);

		assertEquals(IssueStatus.IN_PROGRESS, result.getStatus());
		verify(issueRepository).save(any(Issue.class));
	}

	@Test
	void createIssue_ThrowsWhenProjectDoesNotExist() {
		IssueRequest request = createRequest(null, null, null);
		when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> issueService.createIssue(projectId, request));

		assertEquals("Project not found.", exception.getMessage());
		verify(issueRepository, never()).save(any(Issue.class));
	}

	@Test
	void createIssue_AssociatesConvertedTags() {
		UUID tagOneId = UUID.randomUUID();
		UUID tagTwoId = UUID.randomUUID();

		TagResponse tagOne = TagResponse.builder()
				.projectService(projectService)
				.id(tagOneId)
				.name("backend")
				.color("#112233")
				.projectId(projectId)
				.build();
		TagResponse tagTwo = TagResponse.builder()
				.projectService(projectService)
				.id(tagTwoId)
				.name("frontend")
				.color("#445566")
				.projectId(projectId)
				.build();

		IssueRequest request = createRequest(null, List.of(tagOne, tagTwo), null);
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
		when(projectService.getProjectById(projectId)).thenReturn(project);
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectId, request);

		assertNotNull(result.getTags());
		assertEquals(2, result.getTags().size());

		Tag firstTag = result.getTags().get(0);
		Tag secondTag = result.getTags().get(1);

		assertEquals(tagOneId, firstTag.getId());
		assertEquals("backend", firstTag.getName());
		assertEquals("#112233", firstTag.getColor());
		assertEquals(project, firstTag.getProject());

		assertEquals(tagTwoId, secondTag.getId());
		assertEquals("frontend", secondTag.getName());
		assertEquals("#445566", secondTag.getColor());
		assertEquals(project, secondTag.getProject());
	}

	@Test
	void createIssue_AssignsIssueToRequestedProjectOnly() {
		UUID otherProjectId = UUID.randomUUID();
		Project otherProject = Project.builder()
				.id(otherProjectId)
				.name("Other Project")
				.description("different project")
				.build();

		IssueRequest request = createRequest(null, null, null);
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectId, request);

		assertEquals(projectId, result.getProject().getId());
		assertEquals(project, result.getProject());
        assertNotEquals(otherProjectId, result.getProject().getId());
        assertNotEquals(otherProject, result.getProject());
	}

	@Test
	void createIssue_IgnoresNullTagEntries() {
		IssueRequest request = createRequest(null, Collections.singletonList(null), null);
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectId, request);

		assertNotNull(result.getTags());
		assertEquals(0, result.getTags().size());
	}

	@Test
	void createIssue_LeavesTagsEmptyWhenListIsEmpty() {
		IssueRequest request = createRequest(null, List.of(), null);
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectId, request);

		assertNotNull(result.getTags());
		assertEquals(0, result.getTags().size());
	}

	@Test
	void createIssue_LeavesAssigneeEmptyWhenUsernameIsNull() {
		IssueRequest request = createRequest(null, null, null);
		when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectId, request);

		assertNull(result.getAssignee());
	}

    @Test
    void createIssue_AssignsAssigneeWhenUsernameIsProvided() {
        User assignee = createUser("johndoe");
        IssueRequest request = createRequest(null, null, assignee.getUsername());
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername(assignee.getUsername())).thenReturn(Optional.of(assignee));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.createIssue(projectId, request);
        
        assertNotNull(result.getAssignee());
        assertEquals(assignee.getUsername(), result.getAssignee().getUsername());
        assertEquals(assignee.getId(), result.getAssignee().getId());
    }

	@Test
	void createIssue_SavesAttachmentsWhenProvided() {
		AttachmentMetadataRequest meta1 = new AttachmentMetadataRequest();
		meta1.setOriginalFileName("report.pdf");
		meta1.setBlobFileName("uuid-report.pdf");
		meta1.setFileSize(2048L);
		meta1.setExtension(".pdf");

		AttachmentMetadataRequest meta2 = new AttachmentMetadataRequest();
		meta2.setOriginalFileName("screenshot.png");
		meta2.setBlobFileName("uuid-screenshot.png");
		meta2.setFileSize(4096L);
		meta2.setExtension(".png");

		IssueRequest request = createRequest(null, null, null);
		request.setAttachments(List.of(meta1, meta2));

		when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectId, request);

		assertNotNull(result);
		verify(attachmentRepository, times(2)).save(any(Attachment.class));
	}

	@Test
	void createIssue_DoesNotSaveAttachmentsWhenListIsNull() {
		IssueRequest request = createRequest(null, null, null);
		request.setAttachments(null);

		when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		issueService.createIssue(projectId, request);

		verify(attachmentRepository, never()).save(any(Attachment.class));
	}

	@Test
	void createIssue_DoesNotSaveAttachmentsWhenListIsEmpty() {
		IssueRequest request = createRequest(null, null, null);
		request.setAttachments(List.of());

		when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		issueService.createIssue(projectId, request);

		verify(attachmentRepository, never()).save(any(Attachment.class));
	}

	private IssueRequest createRequest(IssueStatus status, List<TagResponse> tags, String assigneeUsername) {
		IssueRequest request = new IssueRequest();
		request.setTitle("Issue title");
		request.setDescription("Issue description");
		request.setStatus(status);
		request.setPriority(IssuePriority.MEDIUM);
		request.setType(IssueType.BUG);
		request.setTags(tags);
		request.setAssigneeUsername(assigneeUsername);
		return request;
	}

    private User createUser(String username) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(username + "@example.com")
                .build();
        return user;
    }
}
