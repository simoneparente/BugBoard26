package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    // Retrieve all tags associated with a given project
    List<Tag> findByProjectId(UUID projectId);

    // Check if a tag with a given name already exists within a specific project
    // Useful during creation validation (prevents duplicates within the same project)
    boolean existsByNameAndProjectId(String name, UUID projectId);

    // Retrieve a specific tag by name and project ID
    Optional<Tag> findByNameAndProjectId(String name, UUID projectId);
}