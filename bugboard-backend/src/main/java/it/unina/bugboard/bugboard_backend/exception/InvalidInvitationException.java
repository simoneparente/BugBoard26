package it.unina.bugboard.bugboard_backend.exception;

public class InvalidInvitationException extends RuntimeException {
    public InvalidInvitationException(String message) {
        super(message);
    }
}
