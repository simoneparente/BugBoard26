package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.Issue;

public class InProgress extends BaseStatus {
    @Override
    public String getName(){
        return "IN_PROGRESS";
    }
    @Override
    public Status previousStatus() {
        return new Assigned();
    }

    @Override
    public void markForReview(Issue issue) {
        issue.setStatus(new MarkedForReview());
    }

    @Override
    public void close(Issue issue) {
        executeClose(issue);
    }
}
