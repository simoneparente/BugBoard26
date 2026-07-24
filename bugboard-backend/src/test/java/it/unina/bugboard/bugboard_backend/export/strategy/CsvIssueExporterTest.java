package it.unina.bugboard.bugboard_backend.export.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.export.ExportFormat;

class CsvIssueExporterTest {

    private CsvIssueExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new CsvIssueExporter();
    }

    @Test
    void export_ReturnsCsvWithBomAndData() {
        IssueResponse issue = IssueResponse.builder()
                .id(UUID.randomUUID())
                .title("Test Issue")
                .description("Test Description")
                .status("TO_DO")
                .priority("MEDIUM")
                .type("BUG")
                .assigneeUsername("testuser")
                .createdAt(LocalDateTime.of(2023, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2023, 1, 1, 12, 0))
                .projectName("Test Project")
                .tags(List.of(TagResponse.builder().name("bug").build()))
                .build();

        byte[] result = exporter.export(List.of(issue));

        assertEquals((byte) 0xEF, result[0]);
        assertEquals((byte) 0xBB, result[1]);
        assertEquals((byte) 0xBF, result[2]);

        String csvContent = new String(result, 3, result.length - 3, StandardCharsets.UTF_8);
        assertTrue(csvContent.startsWith("ID;Title;Description;Status;Priority;Type;Assignee;Created At;Updated At;Project;Tags"));
        assertTrue(csvContent.contains(issue.getId().toString()));
        assertTrue(csvContent.contains(issue.getTitle()));
        assertTrue(csvContent.endsWith("\n"));
    }

    @Test
    void export_HandlesEmptyList() {
        byte[] result = exporter.export(List.of());

        assertEquals((byte) 0xEF, result[0]);
        assertEquals((byte) 0xBB, result[1]);
        assertEquals((byte) 0xBF, result[2]);

        String csvContent = new String(result, 3, result.length - 3, StandardCharsets.UTF_8);
        assertEquals("ID;Title;Description;Status;Priority;Type;Assignee;Created At;Updated At;Project;Tags\n", csvContent);
    }

    @Test
    void export_HandlesMultipleIssues() {
        IssueResponse firstIssue = IssueResponse.builder()
                .id(UUID.randomUUID())
                .title("First Issue")
                .description("First Description")
                .status("TO_DO")
                .priority("MEDIUM")
                .type("BUG")
                .assigneeUsername("firstuser")
                .createdAt(LocalDateTime.of(2023, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2023, 1, 1, 12, 0))
                .projectName("Project One")
                .tags(List.of())
                .build();

        IssueResponse secondIssue = IssueResponse.builder()
                .id(UUID.randomUUID())
                .title("Second Issue")
                .description("Second Description")
                .status("DONE")
                .priority("HIGH")
                .type("TASK")
                .assigneeUsername("otheruser")
                .createdAt(LocalDateTime.of(2023, 1, 2, 9, 0))
                .updatedAt(LocalDateTime.of(2023, 1, 2, 10, 0))
                .projectName("Project Two")
                .tags(List.of())
                .build();

        byte[] result = exporter.export(List.of(firstIssue, secondIssue));

        String csvContent = new String(result, 3, result.length - 3, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains(firstIssue.getId().toString()));
        assertTrue(csvContent.contains(firstIssue.getTitle()));
        assertTrue(csvContent.contains(secondIssue.getId().toString()));
        assertTrue(csvContent.contains(secondIssue.getTitle()));
        assertEquals(3, csvContent.lines().count());
    }

    @Test
    void export_HandlesNullAndSpecialValues() {
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
                .createdAt(LocalDateTime.of(2023, 1, 3, 9, 0))
                .updatedAt(LocalDateTime.of(2023, 1, 3, 10, 0))
                .projectName("Project X")
                .tags(null)
                .build();

        byte[] result = exporter.export(List.of(specialIssue, nullTagsIssue));

        String csvContent = new String(result, 3, result.length - 3, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("\"value;with;semi\""));
        assertTrue(csvContent.contains("\"quote\"\"inside\nline\""));
        assertTrue(csvContent.contains("bug; feature"));
        assertTrue(csvContent.contains("Plain title"));
        assertTrue(csvContent.contains("Project X"));
    }

    @Test
    void getSupportedFormat_ReturnsCsv() {
        assertEquals(ExportFormat.CSV, exporter.getSupportedFormat());
    }
}
