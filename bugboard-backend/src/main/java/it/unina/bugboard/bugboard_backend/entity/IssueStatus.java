package it.unina.bugboard.bugboard_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "issue_statuses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Il nome dello stato (es. "Open", "In Progress", "Closed")
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    // Un colore esadecimale opzionale per l'interfaccia frontend (es. "#FF0000")
    @Column(length = 7)
    private String colorCode;
}