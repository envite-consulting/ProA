package de.envite.proa.exceptions;

import de.envite.proa.usecases.authentication.exceptions.EmailAlreadyRegisteredException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EmailAlreadyRegisteredExceptionTest {

	private static final String EXPECTED_MESSAGE = "Email already registered: ";
	private static final String EMAIL = "test@test.de";

	@Test
	void test() {
		EmailAlreadyRegisteredException exception = new EmailAlreadyRegisteredException(EMAIL);

		assertNotNull(exception, "Exception should not be null.");
		assertEquals(EXPECTED_MESSAGE + EMAIL, exception.getMessage(),
				"Default message should match the expected value.");
	}
}
