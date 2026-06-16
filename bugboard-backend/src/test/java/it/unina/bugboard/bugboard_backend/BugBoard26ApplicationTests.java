package it.unina.bugboard.bugboard_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BugBoard26ApplicationTests {

	@Test
	void contextLoads() {
		// Verify that the Spring application context loads successfully.
		org.junit.jupiter.api.Assertions.assertTrue(true, "Context should load without errors");
	}

}
