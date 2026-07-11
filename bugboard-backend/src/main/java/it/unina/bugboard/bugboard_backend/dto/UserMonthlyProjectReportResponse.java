package it.unina.bugboard.bugboard_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMonthlyProjectReportResponse {
    private UUID userId;
    private String userName;
    private Integer referenceMonth;
    private Integer referenceYear;
    private Integer openedBugs;
    private Integer managedBugs;
    private Double averageResolutionTime;
}
