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
        executeStartProgress(issue);
    }

    @Override 
    public void close(Issue issue){
        executeClose(issue);
    }

    @Override
    public void removeAssignee(Issue issue){
        executeRemoveAssignee(issue);
    }
    
}
