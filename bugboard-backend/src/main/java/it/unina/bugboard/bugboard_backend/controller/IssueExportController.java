package it.unina.bugboard.bugboard_backend.controller;

import java.util.List;

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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import it.unina.bugboard.bugboard_backend.dto.IssueResponse;
import it.unina.bugboard.bugboard_backend.export.ExportFormat;
import it.unina.bugboard.bugboard_backend.export.IssueExportFactory;
import it.unina.bugboard.bugboard_backend.export.strategy.StreamingIssueExporterStrategy;
import it.unina.bugboard.bugboard_backend.service.IssueService;

@RestController
@RequestMapping("/api/projects/{projectKey}/issues/export")
public class IssueExportController {

    private final IssueExportFactory exportFactory;
    private final IssueService issueService;

    public IssueExportController(IssueExportFactory exportFactory, IssueService issueService) {
        this.exportFactory = exportFactory;
        this.issueService = issueService;
    }

    @GetMapping
    public ResponseEntity<StreamingResponseBody> exportIssues(
            @PathVariable String projectKey,
            @RequestParam(name = "format") String formatParam) {

        ExportFormat format = parseFormat(formatParam);
        StreamingIssueExporterStrategy exporter = exportFactory.getStreamingExporter(format);

        // Streaming response body - writes directly to response stream
        StreamingResponseBody responseBody = output -> {
            StreamingIssueExporterStrategy.IssueStreamFetcher fetcher = pageProcessor -> {
                int pageNumber = 0;
                int pageSize = 500; // Chunk size - configurable
                boolean hasMore = true;

                while (hasMore) {
                    Pageable pageable = PageRequest.of(pageNumber, pageSize);
                    Page<IssueResponse> issuesPage = issueService.getExportIssuesByProjectKey(
                            projectKey, pageable);

                    List<IssueResponse> pageIssues = issuesPage.getContent();

                    if (!pageIssues.isEmpty()) {
                        pageProcessor.processPage(pageIssues);
                    }

                    hasMore = issuesPage.hasNext();
                    pageNumber++;
                }
            };

            exporter.exportStream(output, fetcher);
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, getContentType(format))
                .header(HttpHeaders.CONTENT_DISPOSITION, getContentDisposition(format))
                .body(responseBody);
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
