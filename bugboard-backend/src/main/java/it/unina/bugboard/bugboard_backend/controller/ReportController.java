package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.MonthlyProjectReportResponse;
import it.unina.bugboard.bugboard_backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // Endpoint to generate a report for a specific project
    // Responds to: GET http://localhost:8080/api/reports/{projectId}
    @GetMapping("/{projectKey}")
    public ResponseEntity<MonthlyProjectReportResponse> generateReport(@PathVariable String projectKey) {
        MonthlyProjectReportResponse response = reportService.generateReport(projectKey);
        return ResponseEntity.ok(response);
    }
}
