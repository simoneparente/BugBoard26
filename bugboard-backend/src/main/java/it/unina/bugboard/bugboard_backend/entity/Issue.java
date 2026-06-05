package it.unina.bugboard.bugboard_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "issues")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Campo fantoccio per non avere l'entità vuota
    @Column(nullable = false)
    private String title;

    // Relazione Molti-a-Molti con Tag (Tabella di Join)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "issue_tags",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;
}