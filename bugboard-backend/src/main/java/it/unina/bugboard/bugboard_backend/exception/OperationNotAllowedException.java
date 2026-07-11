package it.unina.bugboard.bugboard_backend.exception;

public class OperationNotAllowedException extends RuntimeException {
    public OperationNotAllowedException(String message) {
        super(message);
    }

    public OperationNotAllowedException(Throwable cause) {
        super(cause);
    }

    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
