package de.envite.proa.exceptions;

import de.envite.proa.usecases.authentication.exceptions.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InvalidPasswordExceptionTest {

	private static final String EXPECTED_MESSAGE = "Invalid password provided.";

	@Test
	void test() {
		InvalidPasswordException exception = new InvalidPasswordException();

		assertNotNull(exception, "Exception should not be null.");
		assertEquals(EXPECTED_MESSAGE, exception.getMessage(), "Default message should match the expected value.");
	}
}
