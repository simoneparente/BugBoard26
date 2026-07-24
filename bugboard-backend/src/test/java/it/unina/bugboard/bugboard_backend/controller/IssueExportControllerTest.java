package it.unina.bugboard.bugboard_backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.Tag;
import it.unina.bugboard.bugboard_backend.entity.User;
import it.unina.bugboard.bugboard_backend.export.ExportFormat;
import it.unina.bugboard.bugboard_backend.export.IssueExportFactory;
import it.unina.bugboard.bugboard_backend.export.strategy.StreamingCsvIssueExporter;
import it.unina.bugboard.bugboard_backend.mapper.IssueMapper;
import it.unina.bugboard.bugboard_backend.service.IssueService;

@ExtendWith(MockitoExtension.class)
class IssueExportControllerTest {

    @Mock
    private IssueExportFactory exportFactory;

    @Mock
    private IssueService issueService;

    @Mock
    private IssueMapper issueMapper;

    @InjectMocks
    private IssueExportController controller;

    private String projectKey;
    private Issue issue;
    private IssueResponse issueResponse;

    @BeforeEach
    void setUp() {
        projectKey = "PRJ";

        Project project = Project.builder()
                .id(UUID.randomUUID())
                .key(projectKey)
                .name("Demo project")
                .build();

        issue = Issue.builder()
                .id(UUID.randomUUID())
                .title("Export issue")
                .description("Issue used for export tests")
                .project(project)
                .assignee(User.builder().username("alice").build())
                .build();

        Tag tag = Tag.builder()
                .id(UUID.randomUUID())
                .name("backend")
                .project(project)
                .build();
        issue.setTags(List.of(tag));

        issueResponse = IssueResponse.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .status("TO_DO")
                .priority("MEDIUM")
                .type("BUG")
                .assigneeUsername("alice")
                .createdAt(LocalDateTime.of(2026, Month.JANUARY, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, Month.JANUARY, 1, 12, 0))
                .projectName("Demo project")
                .tags(List.of())
                .build();
    }

    @Test
    void exportIssues_WithCsvFormat_ReturnsStreamingResponseEntity() throws IOException {
        StreamingCsvIssueExporter realExporter = new StreamingCsvIssueExporter();
        when(exportFactory.getStreamingExporter(ExportFormat.CSV)).thenReturn(realExporter);
        when(issueService.getExportIssuesByProjectKey(eq(projectKey), any(Pageable.class)))
                .thenAnswer(invocation -> emptyIssueResponsePage(invocation.getArgument(1)));

        ResponseEntity<StreamingResponseBody> response = controller.exportIssues(projectKey, "CSV");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("text/csv; charset=UTF-8", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("attachment; filename=\"issues_"));
        assertTrue(contentDisposition.endsWith(".csv\""));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        response.getBody().writeTo(output);

        byte[] result = output.toByteArray();
        assertEquals((byte) 0xEF, result[0]);
        assertEquals((byte) 0xBB, result[1]);
        assertEquals((byte) 0xBF, result[2]);

        String csv = new String(result, 3, result.length - 3, StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("ID;Title;Description;Status;Priority;Type;Assignee;Created At;Updated At;Project;Tags\n"));
        assertEquals("ID;Title;Description;Status;Priority;Type;Assignee;Created At;Updated At;Project;Tags\n", csv);

        verify(exportFactory).getStreamingExporter(ExportFormat.CSV);
        verify(issueService).getExportIssuesByProjectKey(eq(projectKey), any(Pageable.class));
    }

    @Test
    void exportIssues_WithInvalidFormat_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> controller.exportIssues(projectKey, "JSON"));

        assertEquals("Invalid export format: JSON", exception.getMessage());
    }

    @Test
    void exportIssues_WhenFirstPageHasDataAndSecondIsEmpty_ProcessesOnlyNonEmptyPages() throws IOException {
        StreamingCsvIssueExporter realExporter = new StreamingCsvIssueExporter();
        when(exportFactory.getStreamingExporter(ExportFormat.CSV)).thenReturn(realExporter);
        when(issueService.getExportIssuesByProjectKey(eq(projectKey), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    Pageable pageable = invocation.getArgument(1);
                    if (pageable.getPageNumber() == 0) {
                        return new PageImpl<>(List.of(issueResponse), pageable, 501);
                    }
                    if (pageable.getPageNumber() == 1) {
                        return new PageImpl<>(List.of(), pageable, 501);
                    }
                    throw new AssertionError("Unexpected page request: " + pageable);
                });

        ResponseEntity<StreamingResponseBody> response = controller.exportIssues(projectKey, "CSV");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        response.getBody().writeTo(output);

        String csv = new String(output.toByteArray(), 3, output.size() - 3, StandardCharsets.UTF_8);
        assertTrue(csv.contains(issueResponse.getId().toString()));
        assertTrue(csv.contains("Export issue"));
        verify(issueService, times(2)).getExportIssuesByProjectKey(eq(projectKey), any(Pageable.class));
    }

    private Page<IssueResponse> emptyIssueResponsePage(Pageable pageable) {
        return new PageImpl<>(List.of(), pageable, 0);
    }
}