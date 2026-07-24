package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.dto.MonthlyProjectReportResponse;
import it.unina.bugboard.bugboard_backend.entity.*;
import it.unina.bugboard.bugboard_backend.exception.ResourceNotFoundException;
import it.unina.bugboard.bugboard_backend.repository.IssueRepository;
import it.unina.bugboard.bugboard_backend.repository.MonthlyProjectReportRepository;
import it.unina.bugboard.bugboard_backend.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private MonthlyProjectReportRepository monthlyProjectReportRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void generateReport_Success() {
        // 1. ARRANGE
        UUID projectId = UUID.randomUUID();
        Project mockProject = Project.builder()
                .id(projectId)
                .key("FRONT")
                .name("Test Project")
                .build();

        User mockUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .build();

        // We create an issue that was resolved in 2 hours
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime updated = created.plusHours(2);
        Issue resolvedIssue = Issue.builder()
                .id(UUID.randomUUID())
                .status(IssueStatus.COMPLETED)
                .createdAt(created)
                .updatedAt(updated)
                .build();

        // Project exists
        when(projectRepository.findByKey("FRONT")).thenReturn(Optional.of(mockProject));

        // Project metrics
        when(issueRepository.countByProjectIdAndStatusNotInAndCreatedAtBetween(eq(projectId), any(), any(), any()))
                .thenReturn(5L);
        when(issueRepository.countByProjectIdAndStatusAndUpdatedAtBetween(eq(projectId), eq(IssueStatus.COMPLETED), any(), any()))
                .thenReturn(3L);
        when(issueRepository.findByProjectIdAndStatusAndUpdatedAtBetween(eq(projectId), eq(IssueStatus.COMPLETED), any(), any()))
                .thenReturn(List.of(resolvedIssue)); // 1 issue resolved in 2 hours -> avg 2.0

        // User metrics
        when(issueRepository.findDistinctUsersInvolvedInMonth(eq(projectId), any(), any(), eq(IssueStatus.COMPLETED)))
                .thenReturn(List.of(mockUser));
        when(issueRepository.countByProjectIdAndAssigneeIdAndStatusNotInAndCreatedAtBetween(eq(projectId), eq(mockUser.getId()), any(), any(), any()))
                .thenReturn(2L);
        when(issueRepository.countByProjectIdAndAssigneeIdAndStatusAndUpdatedAtBetween(eq(projectId), eq(mockUser.getId()), eq(IssueStatus.COMPLETED), any(), any()))
                .thenReturn(1L);
        when(issueRepository.findByProjectIdAndAssigneeIdAndStatusAndUpdatedAtBetween(eq(projectId), eq(mockUser.getId()), eq(IssueStatus.COMPLETED), any(), any()))
                .thenReturn(List.of(resolvedIssue)); // Avg 2.0

        // Mock saves
        when(monthlyProjectReportRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(monthlyProjectReportRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        // 2. ACT
        MonthlyProjectReportResponse response = reportService.generateReport("FRONT");

        // 3. ASSERT
        assertNotNull(response);
        assertEquals("FRONT", response.getProjectKey());
        assertEquals("Test Project", response.getProjectName());
        assertEquals(5, response.getOpenedBugs());
        assertEquals(3, response.getManagedBugs());
        assertEquals(2.0, response.getAverageResolutionTime()); // 2 hours

        assertEquals(1, response.getUserMonthlyReports().size());
        assertEquals(mockUser.getId(), response.getUserMonthlyReports().get(0).getUserId());
        assertEquals("testuser", response.getUserMonthlyReports().get(0).getUserName());
        assertEquals(2, response.getUserMonthlyReports().get(0).getOpenedBugs());
        assertEquals(1, response.getUserMonthlyReports().get(0).getManagedBugs());
        assertEquals(2.0, response.getUserMonthlyReports().get(0).getAverageResolutionTime());

        verify(monthlyProjectReportRepository, times(1)).save(any(MonthlyProjectReport.class));
        verify(monthlyProjectReportRepository, times(1)).saveAll(anyList());
    }

    @Test
    void generateReport_ThrowsException_WhenProjectNotFound() {
        // ARRANGE
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findByKey("FRONT")).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            reportService.generateReport("FRONT");
        });

        assertTrue(exception.getMessage().contains("not found"));
        verify(issueRepository, never()).countByProjectIdAndStatusNotInAndCreatedAtBetween(any(), any(), any(), any());
        verify(monthlyProjectReportRepository, never()).save(any());
    }

    @Test
    void calculateAverageResolutionTime_ReturnsZeroWhenEmpty() {
        // ARRANGE
        UUID projectId = UUID.randomUUID();
        Project mockProject = Project.builder()
                .id(projectId)
                .name("Empty Project")
                .build();

        when(projectRepository.findByKey("FRONT")).thenReturn(Optional.of(mockProject));

        // Return empty lists so avg time is 0.0
        when(issueRepository.findByProjectIdAndStatusAndUpdatedAtBetween(eq(projectId), eq(IssueStatus.COMPLETED), any(), any()))
                .thenReturn(List.of());

        when(issueRepository.findDistinctUsersInvolvedInMonth(eq(projectId), any(), any(), eq(IssueStatus.COMPLETED)))
                .thenReturn(List.of());

        // ACT
        MonthlyProjectReportResponse response = reportService.generateReport("FRONT");

        // ASSERT
        assertEquals(0.0, response.getAverageResolutionTime());
    }
}
