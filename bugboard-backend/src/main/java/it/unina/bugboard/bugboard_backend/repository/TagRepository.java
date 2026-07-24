package it.unina.bugboard.bugboard_backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import it.unina.bugboard.bugboard_backend.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    // Retrieve all tags associated with a given project
    List<Tag> findByProjectId(UUID projectId);
    List<Tag> findByProjectKey(String projectKey);

    // Check if a tag with a given name already exists within a specific project
    // Useful during creation validation (prevents duplicates within the same project)
    boolean existsByNameAndProjectId(String name, UUID projectId);
    boolean existsByNameAndProjectKey(String name, String projectKey);

    // Retrieve a specific tag by name and project ID
    Optional<Tag> findByNameAndProjectId(String name, UUID projectId);
    Optional<Tag> findByNameAndProjectKey(String name, String projectKey);

    /**
     * Remove all associations between a tag and issues in the join table.
     * Executed before tag deletion to prevent foreign key constraint violations.
     */
    @Modifying
    @Query(value = "DELETE FROM issue_tags WHERE tag_id = ?1", nativeQuery = true)
    void removeTagFromAllIssues(UUID tagId);
}