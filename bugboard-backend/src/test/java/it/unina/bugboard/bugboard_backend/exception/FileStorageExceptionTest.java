package it.unina.bugboard.bugboard_backend.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FileStorageExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Storage error message";
        FileStorageException exception = new FileStorageException(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithCause() {
        Throwable cause = new RuntimeException("Root cause IO issue");
        FileStorageException exception = new FileStorageException(cause);
        assertEquals(cause, exception.getCause());
        assertEquals("java.lang.RuntimeException: Root cause IO issue", exception.getMessage());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Failed to store file";
        Throwable cause = new RuntimeException("Disk full");
        FileStorageException exception = new FileStorageException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
