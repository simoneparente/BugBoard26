package it.unina.bugboard.bugboard_backend.exception;

public class UploadDirectoryException extends RuntimeException {

    public UploadDirectoryException(String message) {
        super(message);
    }

    public UploadDirectoryException(Throwable cause) {
        super(cause);
    }

    public UploadDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
