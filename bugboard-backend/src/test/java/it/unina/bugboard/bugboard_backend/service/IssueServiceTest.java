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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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

	private String projectKey;
	private Project project;

	@BeforeEach
	void setUp() {
		projectKey = "FRONT";
		project = Project.builder()
				.id(UUID.randomUUID())
				.key(projectKey)
				.name("BugBoard Core")
				.description("test project")
				.build();
	}

	@Test
	void createIssue_UsesDefaultStatusAndNoAssignee_WhenStatusAndTagsAreMissing() {
		IssueRequest request = createRequest(null, null, null);
		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectKey, request);

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
		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectKey, request);

		assertEquals(IssueStatus.IN_PROGRESS, result.getStatus());
		verify(issueRepository).save(any(Issue.class));
	}

	@Test
	void createIssue_ThrowsWhenProjectDoesNotExist() {
		IssueRequest request = createRequest(null, null, null);
		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> issueService.createIssue(projectKey, request));

		assertEquals("Project not found with key: " + projectKey, exception.getMessage());
		verify(issueRepository, never()).save(any(Issue.class));
	}

	@Test
	void createIssue_AssociatesConvertedTags() {
		UUID tagOneId = UUID.randomUUID();
		UUID tagTwoId = UUID.randomUUID();

		TagResponse tagOne = TagResponse.builder()
			.id(tagOneId)
			.name("backend")
			.color("#112233")
			.projectKey(projectKey)
			.build();
		TagResponse tagTwo = TagResponse.builder()
			.id(tagTwoId)
			.name("frontend")
			.color("#445566")
			.projectKey(projectKey)
			.build();

		IssueRequest request = createRequest(null, List.of(tagOne, tagTwo), null);
		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectKey, request);

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
		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectKey, request);

		assertEquals(project.getId(), result.getProject().getId());
		assertEquals(project, result.getProject());
        assertNotEquals(otherProjectId, result.getProject().getId());
        assertNotEquals(otherProject, result.getProject());
	}

	@Test
	void createIssue_IgnoresNullTagEntries() {
		IssueRequest request = createRequest(null, Collections.singletonList(null), null);
		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectKey, request);

		assertNotNull(result.getTags());
		assertEquals(0, result.getTags().size());
	}

	@Test
	void createIssue_LeavesTagsEmptyWhenListIsEmpty() {
		IssueRequest request = createRequest(null, List.of(), null);
		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectKey, request);

		assertNotNull(result.getTags());
		assertEquals(0, result.getTags().size());
	}

	@Test
	void createIssue_LeavesAssigneeEmptyWhenUsernameIsNull() {
		IssueRequest request = createRequest(null, null, null);
		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectKey, request);

		assertNull(result.getAssignee());
	}

    @Test
    void createIssue_AssignsAssigneeWhenUsernameIsProvided() {
        User assignee = createUser("johndoe");
        IssueRequest request = createRequest(null, null, assignee.getUsername());
        when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername(assignee.getUsername())).thenReturn(Optional.of(assignee));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Issue result = issueService.createIssue(projectKey, request);
        
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

		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Issue result = issueService.createIssue(projectKey, request);

		assertNotNull(result);
		verify(attachmentRepository, times(2)).save(any(Attachment.class));
	}

	@Test
	void createIssue_DoesNotSaveAttachmentsWhenListIsNull() {
		IssueRequest request = createRequest(null, null, null);
		request.setAttachments(null);

		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		issueService.createIssue(projectKey, request);

		verify(attachmentRepository, never()).save(any(Attachment.class));
	}

	@Test
	void createIssue_DoesNotSaveAttachmentsWhenListIsEmpty() {
		IssueRequest request = createRequest(null, null, null);
		request.setAttachments(List.of());

		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

		issueService.createIssue(projectKey, request);

		verify(attachmentRepository, never()).save(any(Attachment.class));
	}

	@Test
	void setStatus_ClearsAssigneeWhenSetToToDo() {
		User user = createUser("johndoe");
		Issue issue = Issue.builder().id(UUID.randomUUID()).project(project).status(IssueStatus.IN_PROGRESS).assignee(user).build();
		when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
		when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.setStatus(issue.getId(), IssueStatus.TO_DO);

		assertEquals(IssueStatus.TO_DO, result.getStatus());
		assertNull(result.getAssignee());
	}

	@Test
	void setStatus_ClearsAssigneeWhenSetToClosed() {
		User user = createUser("johndoe");
		Issue issue = Issue.builder().id(UUID.randomUUID()).project(project).status(IssueStatus.IN_PROGRESS).assignee(user).build();
		when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
		when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.setStatus(issue.getId(), IssueStatus.CLOSED);

		assertEquals(IssueStatus.CLOSED, result.getStatus());
		assertNull(result.getAssignee());
	}

	@Test
	void setStatus_ThrowsWhenSettingInProgressOnUnassignedIssue() {
		Issue issue = Issue.builder().id(UUID.randomUUID()).project(project).status(IssueStatus.TO_DO).assignee(null).build();
		when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));

		assertThrows(it.unina.bugboard.bugboard_backend.exception.OperationNotAllowedException.class,
				() -> issueService.setStatus(issue.getId(), IssueStatus.IN_PROGRESS));
	}

	@Test
	void setStatus_ThrowsWhenClosingCompletedIssue() {
		Issue issue = Issue.builder().id(UUID.randomUUID()).project(project).status(IssueStatus.COMPLETED).assignee(createUser("johndoe")).build();
		when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));

		assertThrows(it.unina.bugboard.bugboard_backend.exception.OperationNotAllowedException.class,
				() -> issueService.setStatus(issue.getId(), IssueStatus.CLOSED));
	}

	@Test
	void assignIssueToUser_ThrowsWhenIssueIsClosed() {
		User user = createUser("johndoe");
		Issue issue = Issue.builder().id(UUID.randomUUID()).project(project).status(IssueStatus.CLOSED).build();
		when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));

		assertThrows(it.unina.bugboard.bugboard_backend.exception.OperationNotAllowedException.class,
				() -> issueService.assignIssueToUser(issue.getId(), user.getId()));
	}

	@Test
	void assignIssueToUser_SuccessWhenIssueIsActive() {
		User user = createUser("johndoe");
		Issue issue = Issue.builder().id(UUID.randomUUID()).project(project).status(IssueStatus.TO_DO).build();
		when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

		Issue result = issueService.assignIssueToUser(issue.getId(), user.getId());

		assertEquals(user, result.getAssignee());
	}

	@Test
	void removeIssueAssignee_ResetsStatusToToDoAndClearsAssignee() {
		User user = createUser("johndoe");
		Issue issue = Issue.builder().id(UUID.randomUUID()).project(project).status(IssueStatus.IN_PROGRESS).assignee(user).build();
		when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
		when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.removeIssueAssignee(issue.getId());

		assertEquals(IssueStatus.TO_DO, result.getStatus());
		assertNull(result.getAssignee());
	}

	@Test
	void removeIssueAssignee_ThrowsWhenIssueIsCompleted() {
		User user = createUser("johndoe");
		Issue issue = Issue.builder().id(UUID.randomUUID()).project(project).status(IssueStatus.COMPLETED).assignee(user).build();
		when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));

		assertThrows(it.unina.bugboard.bugboard_backend.exception.OperationNotAllowedException.class,
				() -> issueService.removeIssueAssignee(issue.getId()));
	}

	@Test
	void rollbackIssueState_RollsBackClosedToToDoAndClearsAssignee() {
		Issue issue = Issue.builder().id(UUID.randomUUID()).project(project).status(IssueStatus.CLOSED).assignee(null).build();
		when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
		when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.rollbackIssueState(issue.getId());

		assertEquals(IssueStatus.TO_DO, result.getStatus());
		assertNull(result.getAssignee());
	}

	@Test
	void rollbackIssueState_RollsBackInProgressToToDoAndClearsAssignee() {
		User user = createUser("johndoe");
		Issue issue = Issue.builder().id(UUID.randomUUID()).project(project).status(IssueStatus.IN_PROGRESS).assignee(user).build();
		when(issueRepository.findById(issue.getId())).thenReturn(Optional.of(issue));
		when(issueRepository.save(any(Issue.class))).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.rollbackIssueState(issue.getId());

		assertEquals(IssueStatus.TO_DO, result.getStatus());
		assertNull(result.getAssignee());
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
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(username + "@example.com")
                .build();
    }

	@Test
	void getIssuesByProjectKey_ReturnsPagedIssues_WhenStatusAndPriorityAreAll() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());

		Issue issue = Issue.builder()
				.id(UUID.randomUUID())
				.title("Issue title")
				.description("Issue description")
				.status(IssueStatus.TO_DO)
				.priority(IssuePriority.MEDIUM)
				.type(IssueType.BUG)
				.project(project)
				.build();

		Page<Issue> pagedResult = new PageImpl<>(List.of(issue));

		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.findByProjectIdAndFilters(project.getId(), null, null, null, pageable)).thenReturn(pagedResult);

		Page<Issue> result = issueService.getIssuesByProjectKey(projectKey, "ALL", "ALL", pageable);

		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals(issue, result.getContent().get(0));
		verify(issueRepository, times(1)).findByProjectIdAndFilters(project.getId(), null, null, null, pageable);
	}

	@Test
	void getIssuesByProjectKey_ReturnsPagedIssues_WhenStatusAndPriorityAreSpecified() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());

		Issue issue = Issue.builder()
				.id(UUID.randomUUID())
				.title("Issue title")
				.description("Issue description")
				.status(IssueStatus.TO_DO)
				.priority(IssuePriority.HIGH)
				.type(IssueType.BUG)
				.project(project)
				.build();

		Page<Issue> pagedResult = new PageImpl<>(List.of(issue));

		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.findByProjectIdAndFilters(project.getId(), IssueStatus.TO_DO, IssuePriority.HIGH, null, pageable))
				.thenReturn(pagedResult);

		Page<Issue> result = issueService.getIssuesByProjectKey(projectKey, "TO_DO", "HIGH", pageable);

		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("Issue title", result.getContent().get(0).getTitle());
		verify(issueRepository, times(1)).findByProjectIdAndFilters(project.getId(), IssueStatus.TO_DO, IssuePriority.HIGH, null, pageable);
	}

	@Test
	void getIssueByProjectKeyAndSequenceNumber_ReturnsIssue_WhenFound() {
		Long sequenceNumber = 1L;
		Issue issue = Issue.builder()
				.id(UUID.randomUUID())
				.sequenceNumber(sequenceNumber)
				.title("Test Issue")
				.project(project)
				.build();

		when(issueRepository.findByProjectKeyAndSequenceNumber(projectKey, sequenceNumber)).thenReturn(issue);

		Issue result = issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);

		assertNotNull(result);
		assertEquals(sequenceNumber, result.getSequenceNumber());
		assertEquals("Test Issue", result.getTitle());
		verify(issueRepository, times(1)).findByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
	}

	@Test
	void getIssueByProjectKeyAndSequenceNumber_ThrowsResourceNotFoundException_WhenNotFound() {
		Long sequenceNumber = 1L;
		when(issueRepository.findByProjectKeyAndSequenceNumber(projectKey, sequenceNumber)).thenReturn(null);

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> issueService.getIssueByProjectKeyAndSequenceNumber(projectKey, sequenceNumber)
		);

		assertEquals("Issue not found with key " + projectKey + "-" + sequenceNumber, exception.getMessage());
		verify(issueRepository, times(1)).findByProjectKeyAndSequenceNumber(projectKey, sequenceNumber);
	}

	@Test
	void getIssuesByProjectKey_ReturnsPagedIssues_WhenOnlyStatusIsSpecified() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
		Issue issue = Issue.builder()
				.id(UUID.randomUUID())
				.title("Status filtered issue")
				.status(IssueStatus.IN_PROGRESS)
				.priority(IssuePriority.MEDIUM)
				.project(project)
				.build();
		Page<Issue> pagedResult = new PageImpl<>(List.of(issue));

		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.findByProjectIdAndFilters(project.getId(), IssueStatus.IN_PROGRESS, null, null, pageable))
				.thenReturn(pagedResult);

		Page<Issue> result = issueService.getIssuesByProjectKey(projectKey, "IN_PROGRESS", "ALL", pageable);

		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("Status filtered issue", result.getContent().get(0).getTitle());
		verify(issueRepository, times(1)).findByProjectIdAndFilters(project.getId(), IssueStatus.IN_PROGRESS, null, null, pageable);
	}

	@Test
	void getIssuesByProjectKey_ReturnsPagedIssues_WhenOnlyPriorityIsSpecified() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
		Issue issue = Issue.builder()
				.id(UUID.randomUUID())
				.title("Priority filtered issue")
				.status(IssueStatus.TO_DO)
				.priority(IssuePriority.HIGHEST)
				.project(project)
				.build();
		Page<Issue> pagedResult = new PageImpl<>(List.of(issue));

		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.findByProjectIdAndFilters(project.getId(), null, IssuePriority.HIGHEST, null, pageable))
				.thenReturn(pagedResult);

		Page<Issue> result = issueService.getIssuesByProjectKey(projectKey, "ALL", "HIGHEST", pageable);

		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("Priority filtered issue", result.getContent().get(0).getTitle());
		verify(issueRepository, times(1)).findByProjectIdAndFilters(project.getId(), null, IssuePriority.HIGHEST, null, pageable);
	}

	@Test
	void getIssuesByProjectKey_ReturnsPagedIssues_WhenSearchKeywordIsSpecified() {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
		Issue issue = Issue.builder()
				.id(UUID.randomUUID())
				.title("NullPointerException in Login")
				.description("User cannot login due to NPE")
				.status(IssueStatus.TO_DO)
				.priority(IssuePriority.HIGH)
				.project(project)
				.build();
		Page<Issue> pagedResult = new PageImpl<>(List.of(issue));

		when(projectRepository.findByKey(projectKey)).thenReturn(Optional.of(project));
		when(issueRepository.findByProjectIdAndFilters(project.getId(), null, null, "%login%", pageable))
				.thenReturn(pagedResult);

		Page<Issue> result = issueService.getIssuesByProjectKey(projectKey, "ALL", "ALL", "Login", pageable);

		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("NullPointerException in Login", result.getContent().get(0).getTitle());
		verify(issueRepository, times(1)).findByProjectIdAndFilters(project.getId(), null, null, "%login%", pageable);
	}

	@Test
	void deleteIssue_Success() {
		UUID issueId = UUID.randomUUID();
		when(issueRepository.existsById(issueId)).thenReturn(true);
		doNothing().when(issueRepository).deleteById(issueId);

		issueService.deleteIssue(issueId);

		verify(issueRepository, times(1)).deleteById(issueId);
	}

	@Test
	void deleteIssue_ThrowsException_WhenNotFound() {
		UUID issueId = UUID.randomUUID();
		when(issueRepository.existsById(issueId)).thenReturn(false);

		assertThrows(IllegalArgumentException.class, () -> issueService.deleteIssue(issueId));
		verify(issueRepository, never()).deleteById(any());
	}
	@Test
	void startIssueProgress_Success() {
		UUID issueId = UUID.randomUUID();
		User user = User.builder().id(UUID.randomUUID()).build();
		Issue issue = Issue.builder().id(issueId).status(IssueStatus.TO_DO).assignee(user).build();
		when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
		when(issueRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.startIssueProgress(issueId);
		assertEquals(IssueStatus.IN_PROGRESS, result.getStatus());
		verify(issueRepository).save(issue);
	}

	@Test
	void startIssueProgress_ThrowsNotFound() {
		UUID issueId = UUID.randomUUID();
		when(issueRepository.findById(issueId)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> issueService.startIssueProgress(issueId));
	}

	@Test
	void acceptIssue_Success() {
		UUID issueId = UUID.randomUUID();
		Issue issue = Issue.builder().id(issueId).status(IssueStatus.IN_PROGRESS).build();
		when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
		when(issueRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.acceptIssue(issueId);
		assertEquals(IssueStatus.MARKED_FOR_REVIEW, result.getStatus());
		verify(issueRepository).save(issue);
	}

	@Test
	void acceptIssue_ThrowsNotFound() {
		UUID issueId = UUID.randomUUID();
		when(issueRepository.findById(issueId)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> issueService.acceptIssue(issueId));
	}

	@Test
	void rollbackIssueState_Success() {
		UUID issueId = UUID.randomUUID();
		Issue issue = Issue.builder().id(issueId).status(IssueStatus.COMPLETED).build();
		when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
		when(issueRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.rollbackIssueState(issueId);
		assertEquals(IssueStatus.MARKED_FOR_REVIEW, result.getStatus());
		verify(issueRepository).save(issue);
	}

	@Test
	void rollbackIssueState_ThrowsNotFound() {
		UUID issueId = UUID.randomUUID();
		when(issueRepository.findById(issueId)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> issueService.rollbackIssueState(issueId));
	}

	@Test
	void removeIssueAssignee_Success() {
		UUID issueId = UUID.randomUUID();
		User user = User.builder().id(UUID.randomUUID()).build();
		Issue issue = Issue.builder().id(issueId).assignee(user).build();
		when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
		when(issueRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.removeIssueAssignee(issueId);
		assertNull(result.getAssignee());
		verify(issueRepository).save(issue);
	}

	@Test
	void assignIssueToUser_Success() {
		UUID issueId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		Issue issue = Issue.builder().id(issueId).build();
		User user = User.builder().id(userId).build();
		when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(issueRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.assignIssueToUser(issueId, userId);
		assertEquals(user, result.getAssignee());
		verify(issueRepository).save(issue);
	}

	@Test
	void assignIssueToUser_ThrowsExceptions() {
		UUID issueId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		
		when(issueRepository.findById(issueId)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> issueService.assignIssueToUser(issueId, userId));
		
		Issue issue = Issue.builder().id(issueId).build();
		when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
		when(userRepository.findById(userId)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> issueService.assignIssueToUser(issueId, userId));
	}

	@Test
	void addTagToIssue_Success() {
		UUID issueId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		Issue issue = Issue.builder().id(issueId).tags(new java.util.ArrayList<>()).build();
		Tag tag = Tag.builder().id(tagId).build();
		
		when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
		when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
		when(issueRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.addTagToIssue(issueId, tagId);
		assertTrue(result.getTags().contains(tag));
		verify(issueRepository).save(issue);
	}

	@Test
	void addTagToIssue_ThrowsExceptions() {
		UUID issueId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		
		when(issueRepository.findById(issueId)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> issueService.addTagToIssue(issueId, tagId));
		
		Issue issue = Issue.builder().id(issueId).build();
		when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
		when(tagRepository.findById(tagId)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> issueService.addTagToIssue(issueId, tagId));
	}

	@Test
	void removeTagFromIssue_Success() {
		UUID issueId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		Tag tag = Tag.builder().id(tagId).build();
		List<Tag> tags = new java.util.ArrayList<>();
		tags.add(tag);
		Issue issue = Issue.builder().id(issueId).tags(tags).build();
		
		when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
		when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
		when(issueRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.removeTagFromIssue(issueId, tagId);
		assertFalse(result.getTags().contains(tag));
		verify(issueRepository).save(issue);
	}

	@Test
	void removeTagFromIssue_ThrowsExceptions() {
		UUID issueId = UUID.randomUUID();
		UUID tagId = UUID.randomUUID();
		
		when(issueRepository.findById(issueId)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> issueService.removeTagFromIssue(issueId, tagId));
		
		Issue issue = Issue.builder().id(issueId).build();
		when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
		when(tagRepository.findById(tagId)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> issueService.removeTagFromIssue(issueId, tagId));
	}

	@Test
	void setStatus_Success() {
		UUID issueId = UUID.randomUUID();
		Issue issue = Issue.builder().id(issueId).status(IssueStatus.TO_DO).build();
		when(issueRepository.findById(issueId)).thenReturn(Optional.of(issue));
		when(issueRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		Issue result = issueService.setStatus(issueId, IssueStatus.COMPLETED);
		assertEquals(IssueStatus.COMPLETED, result.getStatus());
		verify(issueRepository).save(issue);
	}

	@Test
	void setStatus_ThrowsNotFound() {
		UUID issueId = UUID.randomUUID();
		when(issueRepository.findById(issueId)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> issueService.setStatus(issueId, IssueStatus.IN_PROGRESS));
	}

	@Test
	void getIssuesByProjectKey_ThrowsWhenProjectDoesNotExist() {
		String projectDummyKey = "FRONT";
		when(projectRepository.findByKey(projectDummyKey)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> issueService.getIssuesByProjectKey(projectDummyKey, "ALL", "ALL", null, PageRequest.of(0, 10)));
	}

	@Test
	void removeIssueAssignee_ThrowsNotFound() {
		UUID issueId = UUID.randomUUID();
		when(issueRepository.findById(issueId)).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> issueService.removeIssueAssignee(issueId));
	}
}
