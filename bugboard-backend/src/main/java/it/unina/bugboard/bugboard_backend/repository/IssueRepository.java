package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {
    List<Issue> findByProjectId(UUID projectId);
    Issue findByIdAndProjectId(UUID issueId, UUID projectId);

    // Count bugs opened (created) in the month for a project
    long countByProjectIdAndCreatedAtBetween(UUID projectId, LocalDateTime start, LocalDateTime end);

    // Count resolved bugs (status = COMPLETED, updatedAt in range) for a project
    long countByProjectIdAndStatusAndUpdatedAtBetween(UUID projectId, IssueStatus status, LocalDateTime start, LocalDateTime end);

    // Retrieve resolved bugs to calculate average resolution time
    List<Issue> findByProjectIdAndStatusAndUpdatedAtBetween(UUID projectId, IssueStatus status, LocalDateTime start, LocalDateTime end);

    // Count bugs opened in the month for a specific user in a project
    long countByProjectIdAndAssignedToIdAndCreatedAtBetween(UUID projectId, UUID userId, LocalDateTime start, LocalDateTime end);

    // Count resolved bugs in the month for a specific user in a project
    long countByProjectIdAndAssignedToIdAndStatusAndUpdatedAtBetween(UUID projectId, UUID userId, IssueStatus status, LocalDateTime start, LocalDateTime end);

    // Retrieve resolved bugs for a specific user to calculate average time
    List<Issue> findByProjectIdAndAssignedToIdAndStatusAndUpdatedAtBetween(UUID projectId, UUID userId, IssueStatus status, LocalDateTime start, LocalDateTime end);

    // Find all distinct users assigned to issues of a project in a given range
    @Query("SELECT DISTINCT i.assignedTo FROM Issue i WHERE i.project.id = :projectId AND i.assignedTo IS NOT NULL AND " +
            "(i.createdAt BETWEEN :start AND :end OR (i.status = :status AND i.updatedAt BETWEEN :start AND :end))")
    List<it.unina.bugboard.bugboard_backend.entity.User> findDistinctUsersInvolvedInMonth(
            @Param("projectId") UUID projectId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") IssueStatus status);
}
