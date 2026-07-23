package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    
    boolean existsByName(String name);
    
    Optional<Project> findByName(String name);

    Optional<Project> findByKey(String key);

    boolean existsByKey(String key);
    
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.members WHERE p.id = :id")
    Optional<Project> findByIdWithMembers(@Param("id") UUID id);

    boolean existsByIdAndMembersId(UUID projectId, UUID userId);
    
    Page<Project> findByMembersId(UUID userId, Pageable pageable);
}
