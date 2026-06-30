package it.unina.bugboard.bugboard_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import org.hibernate.annotations.Formula;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @ManyToMany(mappedBy = "projects")
    private List<User> members = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tag> tags = new ArrayList<>();

    // Inverse relation with Issue: if I delete the project, I delete its issues
    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Issue> issues = new ArrayList<>();

    @Formula("(SELECT COUNT(*) FROM issues i WHERE i.project_id = id)") //TODO: Capire se necessario o se si può fare in altro modo
    private Integer issueCount;



    // TODO REPORTS

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneId.systemDefault());
        this.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }
    // NOTE: For future updates, like the option to update the project name or description, 
    // we can add setter methods or use a service layer to handle updates 
    // while ensuring data integrity and business rules are followed.
}