package it.unina.bugboard.bugboard_backend.entity.state;

public class Closed extends BaseStatus {
    @Override
    public String getName() {
        return "CLOSED";
    }

    @Override
    public Status previousStatus() {
        return new NotFixed(); 
    }
    
}
