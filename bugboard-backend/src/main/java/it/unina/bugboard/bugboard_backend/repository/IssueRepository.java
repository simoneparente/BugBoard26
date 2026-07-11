package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {
    
    Page<Issue> findByProjectId(UUID projectId, Pageable pageable);
    Issue findByIdAndProjectId(UUID issueId, UUID projectId);
}
