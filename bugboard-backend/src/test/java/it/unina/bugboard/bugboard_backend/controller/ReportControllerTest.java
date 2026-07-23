package it.unina.bugboard.bugboard_backend.controller;

import it.unina.bugboard.bugboard_backend.dto.MonthlyProjectReportResponse;
import it.unina.bugboard.bugboard_backend.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @Test
    void generateReport_Returns200_WhenProjectExists() {
        // ARRANGE
        UUID projectId = UUID.randomUUID();
        MonthlyProjectReportResponse mockResponse = MonthlyProjectReportResponse.builder()
                .projectId(projectId)
                .projectName("Test Project")
                .openedBugs(5)
                .managedBugs(3)
                .build();

        when(reportService.generateReport(projectId)).thenReturn(mockResponse);

        // ACT
        ResponseEntity<MonthlyProjectReportResponse> response = reportController.generateReport(projectId);

        // ASSERT
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(reportService, times(1)).generateReport(projectId);
    }
}
