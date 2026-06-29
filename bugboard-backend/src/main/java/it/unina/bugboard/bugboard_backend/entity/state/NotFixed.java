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
        executeStartProgress(issue);
    }
    //close
    @Override 
    public void close(Issue issue){
        executeClose(issue);
    }
    //
    @Override
    public void removeAssignee(Issue issue){
        executeRemoveAssignee(issue);
    }
}
