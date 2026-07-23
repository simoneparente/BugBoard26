package it.unina.bugboard.bugboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyProjectReportResponse {
    private String projectKey;
    private String projectName;
    private Integer referenceMonth;
    private Integer referenceYear;
    private Integer openedBugs;
    private Integer managedBugs;
    private Double averageResolutionTime;
    private List<UserMonthlyProjectReportResponse> userMonthlyReports;
}
