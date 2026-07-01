package it.unina.bugboard.bugboard_backend.entity.state;

import java.util.List;

public class Closed extends BaseStatus {
    private String name = "CLOSED";
    private final List<String> nextStatuses = List.of();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getNextStatuses() {
        return nextStatuses;
    }
    @Override
    public Status previousStatus() {
        return new NotFixed(); 
    }
    
}
