package it.unina.bugboard.bugboard_backend.entity;

public interface IssueStatus {

    void next(Issue issue);
    void prev(Issue issue);
    String getStatusName();
    
}