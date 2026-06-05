package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    // Recupera tutti i tag associati a un determinato progetto
    List<Tag> findByProjectId(UUID projectId);

    // Verifica se esiste già un tag con un certo nome all'interno di un progetto specifico
    // Molto utile per la validazione in fase di creazione (evita duplicati nello stesso progetto)
    boolean existsByNameAndProjectId(String name, UUID projectId);

    // Recupera un tag specifico cercando per nome e ID del progetto
    Optional<Tag> findByNameAndProjectId(String name, UUID projectId);
}