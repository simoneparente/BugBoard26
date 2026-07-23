package it.unina.bugboard.bugboard_backend.export.strategy;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.dto.TagResponse;
import it.unina.bugboard.bugboard_backend.export.ExportFormat;

@Component
public class CsvIssueExporter implements IssueExporterStrategy {

    private static final String CSV_HEADER = "ID;Title;Description;Status;Priority;Type;Assignee;Created At;Updated At;Project;Tags";
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public byte[] export(List<IssueResponse> issues) {
        StringBuilder csv = new StringBuilder();
        csv.append(CSV_HEADER).append("\n");

        for (IssueResponse issue : issues) {
            csv.append(escapeCsvValue(issue.getId().toString())).append(";");
            csv.append(escapeCsvValue(issue.getTitle())).append(";");
            csv.append(escapeCsvValue(issue.getDescription())).append(";");
            csv.append(escapeCsvValue(issue.getStatus())).append(";");
            csv.append(escapeCsvValue(issue.getPriority())).append(";");
            csv.append(escapeCsvValue(issue.getType())).append(";");
            csv.append(escapeCsvValue(issue.getAssigneeUsername())).append(";");
            csv.append(escapeCsvValue(formatDateTime(issue.getCreatedAt()))).append(";");
            csv.append(escapeCsvValue(formatDateTime(issue.getUpdatedAt()))).append(";");
            csv.append(escapeCsvValue(issue.getProjectName())).append(";");
            csv.append(escapeCsvValue(formatTags(issue))).append("\n");
        }

        byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + csvBytes.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(csvBytes, 0, result, UTF8_BOM.length, csvBytes.length);

        return result;
    }

    @Override
    public ExportFormat getSupportedFormat() {
        return ExportFormat.CSV;
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
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_FORMATTER);
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
