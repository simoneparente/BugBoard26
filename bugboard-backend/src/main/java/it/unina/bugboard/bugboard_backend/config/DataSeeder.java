package it.unina.bugboard.bugboard_backend.config;

import it.unina.bugboard.bugboard_backend.entity.*;
import it.unina.bugboard.bugboard_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final IssueRepository issueRepository;
    private final AttachmentRepository attachmentRepository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SEED_USERNAME:bugboard}")
    private String seedUsername;

    @Value("${SEED_EMAIL:bugboard@bugboard.it}")
    private String seedEmail;

    @Value("${SEED_PASSWORD:bugboard}")
    private String seedPassword;

    @Override
    @Transactional
    public void run(String... args) {
        // Popola i dati demo solo se il database è completamente vuoto
        if (userRepository.count() > 0) {
            return;
        }

        // 1. Create Users
        User admin = userRepository.save(User.builder()
                .username(seedUsername)
                .email(seedEmail)
                .passwordHash(passwordEncoder.encode(seedPassword))
                .role(Role.ADMIN)
                .build());

        User mario = userRepository.save(User.builder()
                .username("mario.rossi")
                .email("mario@bugboard.it")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.TECHNICAL)
                .build());

        User giulia = userRepository.save(User.builder()
                .username("giulia.bianchi")
                .email("giulia@bugboard.it")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.TECHNICAL)
                .build());

        User luca = userRepository.save(User.builder()
                .username("luca.verdi")
                .email("luca@bugboard.it")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.TECHNICAL)
                .build());

        User anna = userRepository.save(User.builder()
                .username("anna.neri")
                .email("anna@bugboard.it")
                .passwordHash(passwordEncoder.encode("password"))
                .role(Role.TECHNICAL)
                .build());

        // 2. Create Projects with assigned Members
        Project bugboardProject = projectRepository.save(Project.builder()
                .name("BugBoard Reborn")
                .description("Piattaforma principale per il tracciamento dei bug e della produttività")
                .members(List.of(admin, mario, giulia, luca))
                .build());

        Project mobileProject = projectRepository.save(Project.builder()
                .name("Mobile App")
                .description("Applicazione mobile per iOS e Android")
                .members(List.of(admin, mario, anna))
                .build());

        // 3. Create Issues in BugBoard Reborn with different priorities and assignees
        // Mario Rossi: 2 active issues -> HIGHEST (5) + HIGH (4) = Workload 9
        issueRepository.save(Issue.builder()
                .title("Fix del bug di autenticazione JWT")
                .description("Gli utenti riscontrano errori 401 intermittenti durante il refresh del token")
                .project(bugboardProject)
                .priority(IssuePriority.HIGHEST)
                .type(IssueType.BUG)
                .status(IssueStatus.IN_PROGRESS)
                .assignee(mario)
                .build());

        issueRepository.save(Issue.builder()
                .title("Ottimizzazione query SQL per i report")
                .description("La generazione dei report mensili è lenta per progetti con molte issue")
                .project(bugboardProject)
                .priority(IssuePriority.HIGH)
                .type(IssueType.FEATURE)
                .status(IssueStatus.IN_PROGRESS)
                .assignee(mario)
                .build());

        // Giulia Bianchi: 1 active issue -> MEDIUM (3) = Workload 3
        issueRepository.save(Issue.builder()
                .title("Aggiornamento della grafica dell'Header")
                .description("Adattare il menu di navigazione ai nuovi standard di accessibilità")
                .project(bugboardProject)
                .priority(IssuePriority.MEDIUM)
                .type(IssueType.FEATURE)
                .status(IssueStatus.IN_PROGRESS)
                .assignee(giulia)
                .build());

        // Luca Verdi: 1 active issue -> LOW (2) = Workload 2
        issueRepository.save(Issue.builder()
                .title("Aggiunta esportazione PDF delle segnalazioni")
                .description("Permettere il download delle issue in formato PDF")
                .project(bugboardProject)
                .priority(IssuePriority.LOW)
                .type(IssueType.FEATURE)
                .status(IssueStatus.TO_DO)
                .assignee(luca)
                .build());

        // Unassigned Issue (To test recommendation during assignment)
        issueRepository.save(Issue.builder()
                .title("Refactoring dei fogli di stile CSS")
                .description("Rimuovere classi duplicate ed unificare la palette di colori")
                .project(bugboardProject)
                .priority(IssuePriority.MEDIUM)
                .type(IssueType.DOCUMENTATION)
                .status(IssueStatus.TO_DO)
                .assignee(null)
                .build());
    }
}
