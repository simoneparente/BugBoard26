package it.unina.bugboard.bugboard_backend.exception;

/**
 * Thrown when attempting to create or update a tag with a name that already exists in the project.
 * This is a duplicate constraint violation.
 */
public class TagDuplicateException extends RuntimeException {
    public TagDuplicateException(String message) {
        super(message);
    }
}
