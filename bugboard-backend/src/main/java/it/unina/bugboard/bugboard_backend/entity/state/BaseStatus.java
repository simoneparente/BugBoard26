package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.Issue;
import it.unina.bugboard.bugboard_backend.entity.User;

public abstract class BaseStatus implements Status {

    @Override
    public Status previousStatus() {
        return this;
    }

    @Override
    public void assign(Issue issue, User user) {
        throw new IllegalStateException("Operazione 'assign' non consentita nello stato " + getName());
    }

    @Override
    public void startProgress(Issue issue) {
        throw new IllegalStateException("Operazione 'startProgress' non consentita nello stato " + getName());
    }

    @Override
    public void markForReview(Issue issue) {
        throw new IllegalStateException("Operazione 'markForReview' non consentita nello stato " + getName());
    }

    @Override
    public void accept(Issue issue) {
        throw new IllegalStateException("Operazione 'accept' non consentita nello stato " + getName());
    }

    @Override
    public void reject(Issue issue) {
        throw new IllegalStateException("Operazione 'reject' non consentita nello stato " + getName());
    }

    @Override
    public void close(Issue issue) {
        throw new IllegalStateException("Operazione 'close' non consentita nello stato " + getName());
    }

    @Override
    public void removeAssignee(Issue issue) {
        throw new IllegalStateException("Operazione 'close' non consentita nello stato " + getName());
    }

    protected void executeStartProgress(Issue issue) {
        issue.setStatus(new InProgress());
    }

    protected void executeRemoveAssignee(Issue issue) {
        issue.setAssignee(null);
        issue.setStatus(new ToBeAssigned());
    }

    protected void executeClose(Issue issue) {
        issue.setStatus(new Closed());
    }
}
