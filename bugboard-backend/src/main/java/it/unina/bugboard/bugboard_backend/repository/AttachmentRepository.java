package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
}