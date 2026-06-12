package it.unina.bugboard.bugboard_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attachments")
@Getter
@Setter // Spesso utile per i file se devi aggiornare path o metadati
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    // Se vuoi tracciare quale utente ha caricato l'allegato:
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
    
}