package it.unina.bugboard.bugboard_backend.entity;

public enum IssueStatus {
    TO_DO,
    IN_PROGRESS,
    MARKED_FOR_REVIEW,
    NOT_FIXED,
    COMPLETED,
    CLOSED;

    public static IssueStatus fromString(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        try {
            return IssueStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("No enum constant for status: " + status);
        }
    }
}