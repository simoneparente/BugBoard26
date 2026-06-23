package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.Issue;

public class NotFixed extends BaseStatus {
    
    @Override
    public String getName(){
        return "NOT_FIXED";
    }

    @Override
    public Status previousStatus(){
        return new MarkedForReview();
    }

    @Override
    public void startProgress(Issue issue){
        issue.setStatus(new InProgress());
    }
    //close
    @Override 
    public void close(Issue issue){
        //chiusura
    }
    //
    @Override
    public void removeAssignee(Issue issue){
        issue.setAssignee(null);
        issue.setStatus(new ToBeAssigned());
    }
}
