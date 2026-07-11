package it.unina.bugboard.bugboard_backend.repository;

import it.unina.bugboard.bugboard_backend.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    // Use pessimistic locking to prevent concurrent registration with the same token
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Invitation> findByToken(String token);

    long deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
