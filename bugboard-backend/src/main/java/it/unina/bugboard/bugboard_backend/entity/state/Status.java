package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.User;

public interface Status {
    String getName();
    Status previousStatus();

    void assign(Issue issue, User user);
    void startProgress(Issue issue);
    void markForReview(Issue issue);
    void accept (Issue issue);
    void reject (Issue issue);
    void close (Issue issue);
    void removeAssignee(Issue issue);
}