package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.IssuePriority;
import it.unina.bugboard.bugboard_backend.entity.IssueType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Query("SELECT COALESCE(MAX(i.sequenceNumber), 0) FROM Issue i WHERE i.project.id = :projectId")
    Long findMaxSequenceNumberByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT i FROM Issue i WHERE i.project.key = :projectKey AND i.sequenceNumber = :sequenceNumber")
    Issue findByProjectKeyAndSequenceNumber(@Param("projectKey") String projectKey, @Param("sequenceNumber") Long sequenceNumber);

    Page<Issue> findByProjectId(UUID projectId, Pageable pageable);

    Page<Issue> findByProjectIdAndStatusAndPriority(UUID projectId, IssueStatus status, IssuePriority priority, Pageable pageable);

    Page<Issue> findByProjectIdAndPriority(UUID projectId, IssuePriority priority, Pageable pageable);

    Page<Issue> findByProjectIdAndStatus(UUID projectId, IssueStatus status, Pageable pageable);

    @Query(value = "SELECT i FROM Issue i LEFT JOIN i.assignee a WHERE i.project.id = :projectId " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND (:priority IS NULL OR i.priority = :priority) " +
            "AND (:type IS NULL OR i.type = :type) " +
            "AND (:searchPattern IS NULL OR LOWER(i.title) LIKE :searchPattern OR (i.description IS NOT NULL AND LOWER(i.description) LIKE :searchPattern))",
           countQuery = "SELECT COUNT(i) FROM Issue i WHERE i.project.id = :projectId " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND (:priority IS NULL OR i.priority = :priority) " +
            "AND (:type IS NULL OR i.type = :type) " +
            "AND (:searchPattern IS NULL OR LOWER(i.title) LIKE :searchPattern OR (i.description IS NOT NULL AND LOWER(i.description) LIKE :searchPattern))")
    Page<Issue> findByProjectIdAndFilters(
            @Param("projectId") UUID projectId,
            @Param("status") IssueStatus status,
            @Param("priority") IssuePriority priority,
            @Param("type") IssueType type,
            @Param("searchPattern") String searchPattern,
            Pageable pageable);
    
    Issue findByIdAndProjectId(UUID issueId, UUID projectId);

    // Count bugs opened (created) in the month for a project excluding specified statuses (COMPLETED, CLOSED)
    long countByProjectIdAndStatusNotInAndCreatedAtBetween(UUID projectId, List<IssueStatus> statuses, LocalDateTime start, LocalDateTime end);

    // Count resolved bugs (status = COMPLETED, updatedAt in range) for a project
    long countByProjectIdAndStatusAndUpdatedAtBetween(UUID projectId, IssueStatus status, LocalDateTime start, LocalDateTime end);

    // Retrieve resolved bugs to calculate average resolution time
    List<Issue> findByProjectIdAndStatusAndUpdatedAtBetween(UUID projectId, IssueStatus status, LocalDateTime start, LocalDateTime end);

    // Count bugs opened in the month for a specific user in a project excluding specified statuses
    long countByProjectIdAndAssigneeIdAndStatusNotInAndCreatedAtBetween(UUID projectId, UUID userId, List<IssueStatus> statuses, LocalDateTime start, LocalDateTime end);

    // Count resolved bugs in the month for a specific user in a project
    long countByProjectIdAndAssigneeIdAndStatusAndUpdatedAtBetween(UUID projectId, UUID userId, IssueStatus status, LocalDateTime start, LocalDateTime end);

    // Retrieve resolved bugs for a specific user to calculate average time
    List<Issue> findByProjectIdAndAssigneeIdAndStatusAndUpdatedAtBetween(UUID projectId, UUID userId, IssueStatus status, LocalDateTime start, LocalDateTime end);

    // Find all distinct users assigned to issues of a project in a given range
    @Query("SELECT DISTINCT i.assignee FROM Issue i WHERE i.project.id = :projectId AND i.assignee IS NOT NULL AND " +
            "(i.createdAt BETWEEN :start AND :end OR (i.status = :status AND i.updatedAt BETWEEN :start AND :end))")
    List<it.unina.bugboard.bugboard_backend.entity.User> findDistinctUsersInvolvedInMonth(
            @Param("projectId") UUID projectId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") IssueStatus status);
    List<Issue> findByProjectIdAndStatusNotIn(UUID projectId, List<IssueStatus> excludeStatuses);
}
