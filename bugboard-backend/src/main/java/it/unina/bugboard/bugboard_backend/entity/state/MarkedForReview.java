package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.Issue;

public class MarkedForReview extends BaseStatus{

    @Override
    public String getName() {
        return "MARKED_FOR_REVIEW";
    }

    @Override
    public void accept(Issue issue) {
        // Se accettato, immagino passi a uno stato "Closed" o "Resolved"
    }

    @Override
    public void reject(Issue issue) {
        issue.setStatus(new InProgress()); // Torna indietro in lavorazione se rifiutato dal QA
    }

    @Override
    public void close(Issue issue) {
        // Logica di chiusura
    }
}
