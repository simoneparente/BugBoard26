package it.unina.bugboard.bugboard_backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum IssuePriority {
    LOWEST,
    LOW,
    MEDIUM,
    HIGH,
    HIGHEST,
    CRITICAL;

    @JsonCreator
    public static IssuePriority fromString(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if ("CRITICAL".equals(normalized)) {
            return HIGHEST;
        }
        for (IssuePriority priority : IssuePriority.values()) {
            if (priority.name().equalsIgnoreCase(normalized)) {
                return priority;
            }
        }
        return HIGHEST;
    }
}