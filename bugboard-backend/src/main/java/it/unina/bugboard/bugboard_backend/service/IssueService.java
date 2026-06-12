package it.unina.bugboard.bugboard_backend.service;

import it.unina.bugboard.bugboard_backend.entity.*;
import it.unina.bugboard.bugboard_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    //private final IssueStatusRepository issueStatusRepository;
    private final TagRepository tagRepository;

    //Iniezione delle dipendenze
    public IssueService(IssueRepository issueRepository, UserRepository userRepository, 
                        ProjectRepository projectRepository, TagRepository tagRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.tagRepository = tagRepository;
    }

    /*creazione issue nel sistema */
    /**
     * @param title Titolo del bug
     * @param description Descrizione del bug
     * @param creatorId ID dell'utente che crea il bug
     * @return L'issue appena creata
     */

   @Transactional
    public Issue createIssue(String title, String description, UUID projectId, UUID creatorId, List<UUID> tagIds) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Progetto non trovato."));

        // Recuperiamo lo stato di default dinamico
       // IssueStatus defaultStatus = issueStatusRepository.findByName("OPEN")
        //        .orElseThrow(() -> new RuntimeException("Stato iniziale 'OPEN' non configurato a database."));

        // Recuperiamo i tag scelti dall'utente
       List<Tag> tags = tagRepository.findAllById(tagIds);

        Issue issue = Issue.builder()
                .title(title)
                .description(description)
                .project(project)
                //.status(defaultStatus)
                .tags(tags)
                .build();

        return issueRepository.save(issue);
    }
    /**
     * Assegna Issue esistente 
     * @param issueId ID dell'issue da assegnare
     * @param assigneeId ID dello sviluppatore a cui assegnare l'issue
     */

    @Transactional
    public Issue assignIssue(UUID issueId, UUID assigneeId){
        Issue issue = issueRepository.findById(issueId).orElseThrow(() -> new RuntimeException("Issue non trovata ID: " + issueId));
        User assignee = userRepository.findById(assigneeId).orElseThrow(() -> new RuntimeException("Assegnatario non trovato ID: " + assigneeId));
        
        issue.setAssignee(assignee);  
        return issueRepository.save(issue);
    }

    /**
     * Ritorna una Issue dato il suo ID
     */

    @Transactional(readOnly = true)
    public Issue getIssueById(UUID id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Issue non trovata con ID: " + id));
    }

    /**
     * Ritorna la lista di tutte le Issue presenti nel sistema.
     */
    @Transactional(readOnly = true)
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    /**
     * Elimina una Issue e, a cascata (CascadeType.ALL), cancella i record dei suoi allegati dal DB.
     */
    @Transactional
    public void deleteIssue(UUID id) {
        if (!issueRepository.existsById(id)) {
            throw new RuntimeException("Impossibile eliminare. Issue non trovata con ID: " + id);
        }
        issueRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Issue> getIssuesByProjectId(UUID projectId) {
        return issueRepository.findByProjectId(projectId);
    }

    //tags
    @Transactional
    public Issue addTagToIssue(UUID issueId, UUID tagId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovatoo ID: " + issueId));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag non trovato ID: " + tagId));

        issue.getTags().add(tag);
        return issueRepository.save(issue);
    }

    @Transactional
    public Issue removeTagFromIssue(UUID issueId, UUID tagId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue non trovatoo ID: " + issueId));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag non trovato ID: " + tagId));

        issue.getTags().remove(tag);
        return issueRepository.save(issue);
    }

    //TODO updateStatus
}
