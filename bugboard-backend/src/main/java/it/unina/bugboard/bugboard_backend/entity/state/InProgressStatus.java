package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.IssueStatus;

public class InProgressStatus implements IssueStatus {
    @Override
    public void next(Issue issue) {
    }

    @Override
    public void prev(Issue issue) {
        issue.setStatus(new OpenStatus());
    }
    
    @Override
    public String getStatusName() {
        return "IN_PROGRESS";
    }
    
}
