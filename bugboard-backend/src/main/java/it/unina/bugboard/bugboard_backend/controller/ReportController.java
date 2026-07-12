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


    /**
     * Generates a monthly report for a specific project.
     * @param projectId
     * @return ResponseEntity containing the MonthlyProjectReportResponse for the specified project.
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<MonthlyProjectReportResponse> generateReport(@PathVariable UUID projectId) {
        MonthlyProjectReportResponse response = reportService.generateReport(projectId);
        return ResponseEntity.ok(response);
    }
}
