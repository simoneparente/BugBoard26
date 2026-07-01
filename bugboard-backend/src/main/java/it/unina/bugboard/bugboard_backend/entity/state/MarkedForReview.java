package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.Issue;

import java.util.List;



public class MarkedForReview extends BaseStatus{
    private String name = "MARKED_FOR_REVIEW";

    private final List<String> nextStatuses = List.of("NOT_FIXED", "COMPLETED", "CLOSED");

    @Override
    public String getName() {
        return name;
    }


    @Override
    public List<String> getNextStatuses() {
        return nextStatuses;
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
