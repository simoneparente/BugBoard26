package it.unina.bugboard.bugboard_backend.entity.state; 

import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.IssueStatus;

public class OpenStatus implements IssueStatus {
    @Override
    public void next(Issue issue) {
        issue.setStatus(new InProgressStatus());
    }

    @Override
    public void prev(Issue issue) {
        // Già all'inizio, non fa nulla o lancia eccezione
    }
    
    @Override
    public String getStatusName() {
        return "OPEN";
    }
}