package it.unina.bugboard.bugboard_backend.exception;

public class FileStorageException extends RuntimeException {
    
    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(Throwable cause) {
        super(cause);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}