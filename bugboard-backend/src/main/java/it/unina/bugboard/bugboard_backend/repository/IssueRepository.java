package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {
    
    List<Issue> findByProjectId(UUID projectId);
    Issue findByIdAndProjectId(UUID issueId, UUID projectId);
}
