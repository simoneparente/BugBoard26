package it.unina.bugboard.bugboard_backend.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class IssueStatusTest {

    @ParameterizedTest
    @EnumSource(IssueStatus.class)
    void fromString_ValidValues_ReturnsCorrectEnum(IssueStatus status) {
        assertEquals(status, IssueStatus.fromString(status.name()));
        assertEquals(status, IssueStatus.fromString(status.name().toLowerCase()));
    }

    @Test
    void fromString_NullStatus_ThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> IssueStatus.fromString(null)
        );
        assertEquals("Status cannot be null or empty", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t", "\n"})
    void fromString_BlankOrEmptyStatus_ThrowsIllegalArgumentException(String blankInput) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> IssueStatus.fromString(blankInput)
        );
        assertEquals("Status cannot be null or empty", exception.getMessage());
    }

    @Test
    void fromString_InvalidStatus_ThrowsIllegalArgumentException() {
        String invalidStatus = "INVALID_STATUS";
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> IssueStatus.fromString(invalidStatus)
        );
        assertEquals("No enum constant for status: " + invalidStatus, exception.getMessage());
    }

    @Test
    void values_ContainsAllExpectedStatuses() {
        IssueStatus[] values = IssueStatus.values();
        assertEquals(6, values.length);
        assertArrayEquals(
                new IssueStatus[]{
                        IssueStatus.TO_DO,
                        IssueStatus.IN_PROGRESS,
                        IssueStatus.MARKED_FOR_REVIEW,
                        IssueStatus.NOT_FIXED,
                        IssueStatus.COMPLETED,
                        IssueStatus.CLOSED
                },
                values
        );
    }
}
