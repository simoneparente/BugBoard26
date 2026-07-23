package it.unina.bugboard.bugboard_backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IssuePriorityTest {

    @Test
    void testGetWeight() {
        assertEquals(1, IssuePriority.LOWEST.getWeight());
        assertEquals(2, IssuePriority.LOW.getWeight());
        assertEquals(3, IssuePriority.MEDIUM.getWeight());
        assertEquals(4, IssuePriority.HIGH.getWeight());
        assertEquals(5, IssuePriority.HIGHEST.getWeight());
    }
}
