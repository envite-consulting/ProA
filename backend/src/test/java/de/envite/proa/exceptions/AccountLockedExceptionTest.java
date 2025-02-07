package de.envite.proa.exceptions;

import de.envite.proa.usecases.authentication.exceptions.AccountLockedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AccountLockedExceptionTest {

	private static final String EXPECTED_MESSAGE = "Account is locked.";

	@Test
	void test() {
		AccountLockedException exception = new AccountLockedException();

		assertNotNull(exception, "Exception should not be null.");
		assertEquals(EXPECTED_MESSAGE, exception.getMessage(), "Default message should match the expected value.");
	}
}
