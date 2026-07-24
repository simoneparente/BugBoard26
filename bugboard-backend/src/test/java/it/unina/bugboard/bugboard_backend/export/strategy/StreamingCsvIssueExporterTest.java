package it.unina.bugboard.bugboard_backend.export.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.export.ExportFormat;

@ExtendWith(MockitoExtension.class)
class StreamingCsvIssueExporterTest {

    @Mock
    private StreamingCsvIssueExporter.IssueStreamFetcher mockFetcher;

    private StreamingCsvIssueExporter exporter;

    private ByteArrayOutputStream outputStream;
    private IssueResponse testIssue;
    private IssueResponse secondIssue;

    @BeforeEach
    void setUp() {
        exporter = new StreamingCsvIssueExporter();
        outputStream = new ByteArrayOutputStream();

        testIssue = IssueResponse.builder()
                .id(UUID.randomUUID())
                .title("Test Issue")
                .description("Test Description")
                .status("TO_DO")
                .priority("MEDIUM")
                .type("BUG")
                .assigneeUsername("testuser")
                .createdAt(LocalDateTime.of(2023, Month.JANUARY, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2023, Month.JANUARY, 1, 12, 0))
                .projectName("Test Project")
                .tags(List.of())
                .build();

        secondIssue = IssueResponse.builder()
                .id(UUID.randomUUID())
                .title("Second Issue")
                .description("Second Description")
                .status("DONE")
                .priority("HIGH")
                .type("TASK")
                .assigneeUsername("otheruser")
                .createdAt(LocalDateTime.of(2023, Month.JANUARY, 2, 9, 0))
                .updatedAt(LocalDateTime.of(2023, Month.JANUARY, 2, 10, 0))
                .projectName("Another Project")
                .tags(List.of(TagResponse.builder().name("backend").build()))
                .build();
    }

    @Test
    void exportStream_WritesCsvHeaderAndBom() {
        doAnswer(invocation -> {
            StreamingCsvIssueExporter.PageProcessor processor = invocation.getArgument(0);
            processor.processPage(List.of(testIssue));
            return null;
        }).when(mockFetcher).fetchPages(any());

        exporter.exportStream(outputStream, mockFetcher);

        byte[] result = outputStream.toByteArray();
        assertEquals((byte) 0xEF, result[0]);
        assertEquals((byte) 0xBB, result[1]);
        assertEquals((byte) 0xBF, result[2]);

        String csvContent = new String(result, 3, result.length - 3, StandardCharsets.UTF_8);
        assertTrue(csvContent.startsWith("ID;Title;Description;Status;Priority;Type;Assignee;Created At;Updated At;Project;Tags"));
        assertTrue(csvContent.contains(testIssue.getId().toString()));
        assertTrue(csvContent.contains(testIssue.getTitle()));
        verify(mockFetcher).fetchPages(any());
    }

    @Test
    void exportStream_HandlesEmptyIssuesList() {
        doAnswer(invocation -> {
            StreamingCsvIssueExporter.PageProcessor processor = invocation.getArgument(0);
            processor.processPage(List.of());
            return null;
        }).when(mockFetcher).fetchPages(any());

        exporter.exportStream(outputStream, mockFetcher);

        byte[] result = outputStream.toByteArray();
        String csvContent = new String(result, 3, result.length - 3, StandardCharsets.UTF_8);
        assertEquals("ID;Title;Description;Status;Priority;Type;Assignee;Created At;Updated At;Project;Tags\n", csvContent);
        verify(mockFetcher).fetchPages(any());
    }

    @Test
    void exportStream_HandlesMultiplePages() {
        doAnswer(invocation -> {
            StreamingCsvIssueExporter.PageProcessor processor = invocation.getArgument(0);
            processor.processPage(List.of(testIssue));
            processor.processPage(List.of(secondIssue));
            return null;
        }).when(mockFetcher).fetchPages(any());

        exporter.exportStream(outputStream, mockFetcher);

        byte[] result = outputStream.toByteArray();
        String csvContent = new String(result, 3, result.length - 3, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains(testIssue.getId().toString()));
        assertTrue(csvContent.contains(secondIssue.getId().toString()));
        assertTrue(csvContent.contains("backend"));
        assertEquals(3, csvContent.lines().count());
        verify(mockFetcher).fetchPages(any());
    }

    @Test
    void exportStream_HandlesNullAndSpecialValues() {
        IssueResponse specialIssue = IssueResponse.builder()
                .id(UUID.randomUUID())
                .title("value;with;semi")
                .description("quote\"inside\nline")
                .status(null)
                .priority("")
                .type("TASK")
                .assigneeUsername(null)
                .createdAt(null)
                .updatedAt(null)
                .projectName(null)
                .tags(List.of(
                        TagResponse.builder().name("bug").build(),
                        TagResponse.builder().name("feature").build()))
                .build();

        IssueResponse nullTagsIssue = IssueResponse.builder()
                .id(UUID.randomUUID())
                .title("Plain title")
                .description("Plain description")
                .status("DONE")
                .priority("HIGH")
                .type("BUG")
                .assigneeUsername("user")
                .createdAt(LocalDateTime.of(2023, Month.JANUARY, 3, 9, 0))
                .updatedAt(LocalDateTime.of(2023, Month.JANUARY, 3, 10, 0))
                .projectName("Project X")
                .tags(null)
                .build();

        doAnswer(invocation -> {
            StreamingCsvIssueExporter.PageProcessor processor = invocation.getArgument(0);
            processor.processPage(List.of(specialIssue));
            processor.processPage(List.of(nullTagsIssue));
            return null;
        }).when(mockFetcher).fetchPages(any());

        exporter.exportStream(outputStream, mockFetcher);

        byte[] result = outputStream.toByteArray();
        String csvContent = new String(result, 3, result.length - 3, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("\"value;with;semi\""));
        assertTrue(csvContent.contains("\"quote\"\"inside\nline\""));
        assertTrue(csvContent.contains("bug; feature"));
        assertTrue(csvContent.contains("Plain title"));
        assertTrue(csvContent.contains("Project X"));
        verify(mockFetcher).fetchPages(any());
    }

    @Test
    void getSupportedFormat_ReturnsCsv() {
        assertEquals(ExportFormat.CSV, exporter.getSupportedFormat());
    }
}
