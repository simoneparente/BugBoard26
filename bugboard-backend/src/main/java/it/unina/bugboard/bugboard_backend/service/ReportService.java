package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.MonthlyProjectReportResponse;
import it.unina.bugboard.bugboard_backend.dto.UserMonthlyProjectReportResponse;
import it.unina.bugboard.bugboard_backend.entity.*;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.IssueRepository;
import it.unina.bugboard.bugboard_backend.repository.MonthlyProjectReportRepository;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final MonthlyProjectReportRepository monthlyProjectReportRepository;

    /**
     * Generates the monthly report for a project (current month).
     *
     * Calculated metrics:
     * - openedBugs: issues created in the month (createdAt in range)
     * - managedBugs: issues with status COMPLETED and updatedAt in range
     * - averageResolutionTime: average in hours of (updatedAt - createdAt) for COMPLETED issues in the month
     *
     * The same metrics are calculated for each user involved in the month.
     * The report is also persisted as MonthlyProjectReport and UserMonthlyProjectReport.
     */
    @Transactional
    public MonthlyProjectReportResponse generateReport(String projectKey) {
        Project project = projectRepository.findByKey(projectKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Project with key %s not found", projectKey)));

        UUID projectId = project.getId();

        // Calculate the current month range
        LocalDate now = LocalDate.now(ZoneId.systemDefault());
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        LocalDateTime monthStart = LocalDateTime.of(now.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime monthEnd = LocalDateTime.of(now.withDayOfMonth(now.lengthOfMonth()), LocalTime.MAX);

        List<IssueStatus> excludedStatuses = List.of(IssueStatus.COMPLETED, IssueStatus.CLOSED);

        // --- Project-level metrics ---
        long openedBugs = issueRepository.countByProjectIdAndStatusNotInAndCreatedAtBetween(
                projectId, excludedStatuses, monthStart, monthEnd);

        long managedBugs = issueRepository.countByProjectIdAndStatusAndUpdatedAtBetween(
                projectId, IssueStatus.COMPLETED, monthStart, monthEnd);

        List<Issue> resolvedIssues = issueRepository.findByProjectIdAndStatusAndUpdatedAtBetween(
                projectId, IssueStatus.COMPLETED, monthStart, monthEnd);

        double avgResolutionTime = calculateAverageResolutionTime(resolvedIssues);

        // --- Per-user metrics ---
        List<User> involvedUsers = issueRepository.findDistinctUsersInvolvedInMonth(
                projectId, monthStart, monthEnd, IssueStatus.COMPLETED);

        List<UserMonthlyProjectReportResponse> userReports = new ArrayList<>();
        List<UserMonthlyProjectReport> userReportEntities = new ArrayList<>();

        for (User user : involvedUsers) {
            long userOpened = issueRepository.countByProjectIdAndAssigneeIdAndStatusNotInAndCreatedAtBetween(
                    projectId, user.getId(), excludedStatuses, monthStart, monthEnd);

            long userManaged = issueRepository.countByProjectIdAndAssigneeIdAndStatusAndUpdatedAtBetween(
                    projectId, user.getId(), IssueStatus.COMPLETED, monthStart, monthEnd);

            List<Issue> userResolved = issueRepository.findByProjectIdAndAssigneeIdAndStatusAndUpdatedAtBetween(
                    projectId, user.getId(), IssueStatus.COMPLETED, monthStart, monthEnd);

            double userAvgTime = calculateAverageResolutionTime(userResolved);

            // DTO for the response
            userReports.add(UserMonthlyProjectReportResponse.builder()
                    .userId(user.getId())
                    .userName(user.getUsername())
                    .referenceMonth(currentMonth)
                    .referenceYear(currentYear)
                    .openedBugs((int) userOpened)
                    .managedBugs((int) userManaged)
                    .averageResolutionTime(userAvgTime)
                    .build());

            // Entity for persistence
            userReportEntities.add(UserMonthlyProjectReport.builder()
                    .project(project)
                    .referenceMonth(currentMonth)
                    .referenceYear(currentYear)
                    .averageResolutionTime(userAvgTime)
                    .user(user)
                    .build());
        }

        // --- Project report persistence ---
        MonthlyProjectReport reportEntity = MonthlyProjectReport.builder()
                .project(project)
                .referenceMonth(currentMonth)
                .referenceYear(currentYear)
                .averageResolutionTime(avgResolutionTime)
                .build();

        monthlyProjectReportRepository.save(reportEntity);
        monthlyProjectReportRepository.saveAll(userReportEntities);

        // --- Response ---
        return MonthlyProjectReportResponse.builder()
                .projectKey(project.getKey())
                .projectName(project.getName())
                .referenceMonth(currentMonth)
                .referenceYear(currentYear)
                .openedBugs((int) openedBugs)
                .managedBugs((int) managedBugs)
                .averageResolutionTime(avgResolutionTime)
                .userMonthlyReports(userReports)
                .build();
    }

    /**
     * Calculates the average resolution time in hours.
     * For each resolved issue, the resolution time is: updatedAt - createdAt.
     *
     * @param resolvedIssues list of issues with status COMPLETED
     * @return average time in hours, or 0.0 if the list is empty
     */
    private double calculateAverageResolutionTime(List<Issue> resolvedIssues) {
        if (resolvedIssues.isEmpty()) {
            return 0.0;
        }

        double totalHours = resolvedIssues.stream()
                .mapToDouble(issue -> {
                    Duration duration = Duration.between(issue.getCreatedAt(), issue.getUpdatedAt());
                    return duration.toMinutes() / 60.0;
                })
                .sum();

        return Math.round((totalHours / resolvedIssues.size()) * 100.0) / 100.0;
    }
}
