package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.Issue;

public class MarkedForReview extends BaseStatus{

    @Override
    public String getName() {
        return "MARKED_FOR_REVIEW";
    }

    @Override
    public void accept(Issue issue) {
        executeAccept(issue);
    }

    @Override
    public Status previousStatus(){
        return new InProgress();
    }

    @Override
    public void reject(Issue issue) {
        issue.setStatus(new InProgress()); 
    }

    @Override
    public void close(Issue issue) {
        executeClose(issue);
    }
}
