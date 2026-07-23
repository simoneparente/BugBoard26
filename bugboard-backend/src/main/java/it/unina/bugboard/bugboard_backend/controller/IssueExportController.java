package it.unina.bugboard.bugboard_backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.export.ExportFormat;
import it.unina.bugboard.bugboard_backend.export.IssueExportFactory;
import it.unina.bugboard.bugboard_backend.export.strategy.IssueExporterStrategy;
import it.unina.bugboard.bugboard_backend.mapper.IssueMapper;
import it.unina.bugboard.bugboard_backend.service.IssueService;

@RestController
@RequestMapping("/api/projects/{projectId}/issues/export")
public class IssueExportController {

    private final IssueExportFactory exportFactory;
    private final IssueService issueService;
    private final IssueMapper issueMapper;

    public IssueExportController(IssueExportFactory exportFactory, IssueService issueService, IssueMapper issueMapper) {
        this.exportFactory = exportFactory;
        this.issueService = issueService;
        this.issueMapper = issueMapper;
    }

    @GetMapping
    public ResponseEntity<byte[]> exportIssues(
            @PathVariable UUID projectId,
            @RequestParam(name = "format") String formatParam) {

        ExportFormat format = parseFormat(formatParam);
        IssueExporterStrategy exporter = exportFactory.getExporter(format);

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<?> issuesPage = issueService.getIssuesByProjectId(projectId, "ALL", "ALL", pageable);

        List<IssueResponse> allIssues = issuesPage.getContent().stream()
                .map(issue -> issueMapper.toResponse((Issue) issue))
                .toList();

        byte[] exportedData = exporter.export(allIssues);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, getContentType(format))
                .header(HttpHeaders.CONTENT_DISPOSITION, getContentDisposition(format))
                .body(exportedData);
    }

    private ExportFormat parseFormat(String formatParam) {
        try {
            return ExportFormat.valueOf(formatParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid export format: " + formatParam);
        }
    }

    private String getContentType(ExportFormat format) {
        return switch (format) {
            case CSV -> "text/csv; charset=UTF-8";
        };
    }

    private String getContentDisposition(ExportFormat format) {
        String fileExtension = format.toString().toLowerCase();
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "attachment; filename=\"issues_" + timestamp + "." + fileExtension + "\"";
    }
}
