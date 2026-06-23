package it.unina.bugboard.bugboard_backend.entity.state;

import it.unina.bugboard.bugboard_backend.entity.User;

import it.unina.bugboard.bugboard_backend.entity.Issue;

public class ToBeAssigned extends BaseStatus {
    @Override 
    public String getName(){
        return "TO_BE_ASSIGNED";
    }

    @Override
    public void assign(Issue issue, User user){
        issue.setAssignee(user);
        issue.setStatus(new Assigned());
    }

    @Override 
    public void close(Issue issue){
        //chiusura 
    }
}