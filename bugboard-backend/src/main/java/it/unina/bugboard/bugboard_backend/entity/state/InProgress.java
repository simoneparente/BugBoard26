package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.Issue;
import java.util.List;

public class InProgress extends BaseStatus {
    private String name = "IN_PROGRESS";

    private final List<String> nextStatuses = List.of("MARKED_FOR_REVIEW", "CLOSED");

    @Override
    public String getName(){
        return name;
    }

    @Override
    public List<String> getNextStatuses(){
        return nextStatuses;
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
