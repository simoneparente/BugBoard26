package it.unina.bugboard.bugboard_backend.export.strategy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.export.ExportFormat;

/**
 * CSV exporter with streaming - writes row by row without accumulating in memory.
 */
@Component
public class StreamingCsvIssueExporter implements StreamingIssueExporterStrategy {

    private static final String CSV_HEADER = "ID;Title;Description;Status;Priority;Type;Assignee;Created At;Updated At;Project;Tags\n";
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exportStream(OutputStream output, IssueStreamFetcher fetchStrategy) {
        try {
            // Write UTF-8 BOM
            output.write(UTF8_BOM);
            output.flush();

            // Write header
            output.write(CSV_HEADER.getBytes(StandardCharsets.UTF_8));
            output.flush();

            // Fetch and write page by page
            fetchStrategy.fetchPages(issues -> {
                writeIssuesToStream(output, issues);
                return true;
            });

            output.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error during CSV export", e);
        }
    }

    @Override
    public ExportFormat getSupportedFormat() {
        return ExportFormat.CSV;
    }

    private void writeIssuesToStream(OutputStream output, List<IssueResponse> issues) {
        try (OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            for (IssueResponse issue : issues) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCsvValue(issue.getId().toString())).append(";");
                line.append(escapeCsvValue(issue.getTitle())).append(";");
                line.append(escapeCsvValue(issue.getDescription())).append(";");
                line.append(escapeCsvValue(issue.getStatus())).append(";");
                line.append(escapeCsvValue(issue.getPriority())).append(";");
                line.append(escapeCsvValue(issue.getType())).append(";");
                line.append(escapeCsvValue(issue.getAssigneeUsername())).append(";");
                line.append(escapeCsvValue(formatDateTime(issue.getCreatedAt()))).append(";");
                line.append(escapeCsvValue(formatDateTime(issue.getUpdatedAt()))).append(";");
                line.append(escapeCsvValue(issue.getProjectName())).append(";");
                line.append(escapeCsvValue(formatTags(issue))).append("\n");

                writer.append(line);
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while writing CSV", e);
        }
    }

    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_FORMATTER);
    }

    private String formatTags(IssueResponse issue) {
        if (issue.getTags() == null || issue.getTags().isEmpty()) {
            return "";
        }
        return issue.getTags().stream()
                .map(TagResponse::getName)
                .collect(Collectors.joining("; "));
    }
}
