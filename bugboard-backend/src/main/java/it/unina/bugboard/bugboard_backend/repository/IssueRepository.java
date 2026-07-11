package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.IssueStatus;
import it.unina.bugboard.bugboard_backend.entity.User;
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

    long countByProjectIdAndCreatedAtBetween(UUID projectId, LocalDateTime start, LocalDateTime end);
    long countByProjectIdAndStatusAndUpdatedAtBetween(UUID projectId, IssueStatus status, LocalDateTime start, LocalDateTime end);
    List<Issue> findByProjectIdAndStatusAndUpdatedAtBetween(UUID projectId, IssueStatus status, LocalDateTime start, LocalDateTime end);

    long countByProjectIdAndAssigneeIdAndCreatedAtBetween(UUID projectId, UUID userId, LocalDateTime start, LocalDateTime end);
    long countByProjectIdAndAssigneeIdAndStatusAndUpdatedAtBetween(UUID projectId, UUID userId, IssueStatus status, LocalDateTime start, LocalDateTime end);
    List<Issue> findByProjectIdAndAssigneeIdAndStatusAndUpdatedAtBetween(UUID projectId, UUID userId, IssueStatus status, LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT i.assignee FROM Issue i WHERE i.project.id = :projectId AND i.assignee IS NOT NULL AND " +
            "(i.createdAt BETWEEN :start AND :end OR (i.status = :status AND i.updatedAt BETWEEN :start AND :end))")
    List<User> findDistinctUsersInvolvedInMonth(
            @Param("projectId") UUID projectId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") IssueStatus status);
}
