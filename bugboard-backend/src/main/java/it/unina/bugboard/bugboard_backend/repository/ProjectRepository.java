package it.unina.bugboard.bugboard_backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.unina.bugboard.bugboard_backend.entity.Project;
import it.unina.bugboard.bugboard_backend.entity.User;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    
    boolean existsByName(String name);
    
    Optional<Project> findByName(String name);
    
    boolean existsByIdAndMembersId(UUID projectId, UUID userId);
    
    Page<Project> findByMembersId(UUID userId, Pageable pageable);

    /**
     * Retrieves paginated members of a specific project.
     * Uses a database query for efficient pagination.
     *
     * @param projectId the project ID
     * @param pageable pagination information
     * @return page of project members
     */
    @Query(value = "SELECT DISTINCT m FROM Project p JOIN p.members m WHERE p.id = :projectId",
           countQuery = "SELECT COUNT(DISTINCT m) FROM Project p JOIN p.members m WHERE p.id = :projectId")
    Page<User> findMembersByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Retrieves available users (not members and not ADMIN) for a specific project.
     * Uses a database query for efficient pagination and filtering.
     *
     * @param projectId the project ID
     * @param pageable pagination information
     * @return page of available users
     */
    @Query(value = "SELECT DISTINCT u FROM User u WHERE " +
                   "u.role <> 'ADMIN' AND " +
                   "u.id NOT IN (SELECT m.id FROM Project p JOIN p.members m WHERE p.id = :projectId)",
           countQuery = "SELECT COUNT(DISTINCT u) FROM User u WHERE " +
                        "u.role <> 'ADMIN' AND " +
                        "u.id NOT IN (SELECT m.id FROM Project p JOIN p.members m WHERE p.id = :projectId)")
    Page<User> findAvailableUsersForProject(@Param("projectId") UUID projectId, Pageable pageable);
}
