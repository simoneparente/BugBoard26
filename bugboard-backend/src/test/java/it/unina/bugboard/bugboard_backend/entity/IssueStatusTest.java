package it.unina.bugboard.bugboard_backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IssueStatusTest {

    @Test
    void fromString_ValidStatus_ReturnsEnum() {
        assertEquals(IssueStatus.TO_DO, IssueStatus.fromString("TO_DO"));
        assertEquals(IssueStatus.IN_PROGRESS, IssueStatus.fromString("in_progress"));
        assertEquals(IssueStatus.MARKED_FOR_REVIEW, IssueStatus.fromString("Marked_For_Review"));
        assertEquals(IssueStatus.NOT_FIXED, IssueStatus.fromString("NOT_FIXED"));
        assertEquals(IssueStatus.COMPLETED, IssueStatus.fromString("completed"));
        assertEquals(IssueStatus.CLOSED, IssueStatus.fromString("Closed"));
    }

    @Test
    void fromString_NullStatus_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> IssueStatus.fromString(null)
        );
        assertEquals("Status cannot be null or empty", exception.getMessage());
    }

    @Test
    void fromString_EmptyStatus_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> IssueStatus.fromString("   ")
        );
        assertEquals("Status cannot be null or empty", exception.getMessage());
    }

    @Test
    void fromString_InvalidStatus_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> IssueStatus.fromString("INVALID_STATUS")
        );
        assertEquals("No enum constant for status: INVALID_STATUS", exception.getMessage());
    }
}
