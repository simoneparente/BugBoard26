package it.unina.bugboard.bugboard_backend.entity;

public enum IssuePriority {
    LOWEST(1),
    LOW(2),
    MEDIUM(3),
    HIGH(4),
    HIGHEST(5);

    private final int weight;

    IssuePriority(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}