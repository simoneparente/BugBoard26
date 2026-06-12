package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    boolean existsByName(String name);
    
    Optional<Project> findByName(String name); 
}
