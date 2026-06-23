package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.Issue;

public class Assigned extends BaseStatus {

    @Override
    public String getName(){
        return "ASSIGNED";
    }

    @Override
    public Status previousStatus(){
        return new ToBeAssigned();
    }

    @Override
    public void startProgress(Issue issue){
        issue.setStatus(new InProgress());
    }

    @Override 
    public void close(Issue issue){
        //chiusura issue
    }

    @Override
    public void removeAssignee(Issue issue){
        issue.setAssignee(null);
        issue.setStatus(new ToBeAssigned());
    }
    
}
