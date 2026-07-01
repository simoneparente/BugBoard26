package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.Issue;
import java.util.List;

public class Assigned extends BaseStatus {
    private String name = "ASSIGNED";

    private final List<String> nextStatuses = List.of("IN_PROGRESS", "CLOSED");

    @Override
    public String getName(){
        return name;
    }

    @Override
    public List<String> getNextStatuses(){
        return nextStatuses;
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
