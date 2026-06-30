package it.unina.bugboard.bugboard_backend.entity;

import it.unina.bugboard.bugboard_backend.entity.state.Status;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Entity
@Table(name = "issues")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    // Owning side of the Project -> issues relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Convert(converter = IssueStatusConverter.class)
    @Column(nullable = false, length = 30)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssuePriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "issue_tags",
            joinColumns = @JoinColumn(name = "issue_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now(ZoneId.systemDefault());
        updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void assign(User user){
        this.status.assign(this, user);
    }

    public void removeAssignee(){
        this.status.removeAssignee(this);
    }

    public void startPorgress(){
        this.status.startProgress(this);
    }

    public void accept(){
        this.status.accept(this);
    }

    public void previousState(){
        this.status = this.status.previousStatus();
    }



}

