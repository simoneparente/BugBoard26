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
        throw new IllegalStateException("Operation 'assign' not allowed in the state " + getName());
    }

    @Override
    public void startProgress(Issue issue) {
        throw new IllegalStateException("Operation 'startProgress' not allowed in the state " + getName());
    }

    @Override
    public void markForReview(Issue issue) {
        throw new IllegalStateException("Operation 'markForReview' not allowed in the state " + getName());
    }

    @Override
    public void accept(Issue issue) {
        throw new IllegalStateException("Operation 'accept' not allowed in the state " + getName());
    }

    @Override
    public void reject(Issue issue) {
        throw new IllegalStateException("Operation 'reject' not allowed in the state " + getName());
    }

    @Override
    public void close(Issue issue) {
        throw new IllegalStateException("Operation 'close' not allowed in the state " + getName());
    }

    @Override
    public void removeAssignee(Issue issue) {
        throw new IllegalStateException("Operation 'close' not allowed in the state " + getName());
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

    protected void executeAccept(Issue issue){
        issue.setStatus(new Closed());
    }
}
