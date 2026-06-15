package it.unina.bugboard.bugboard_backend.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UploadDirectoryExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Could not create upload directory!";
        UploadDirectoryException exception = new UploadDirectoryException(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithCause() {
        Throwable cause = new RuntimeException("Permission denied");
        UploadDirectoryException exception = new UploadDirectoryException(cause);
        assertEquals(cause, exception.getCause());
        assertEquals("java.lang.RuntimeException: Permission denied", exception.getMessage());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Could not create upload directory!";
        Throwable cause = new RuntimeException("Disk full");
        UploadDirectoryException exception = new UploadDirectoryException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
