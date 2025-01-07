package de.envite.proa.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailAlreadyRegisteredExceptionTest {

    private static final String EXPECTED_MESSAGE = "Email already registered: ";
    private static final String EMAIL = "test@test.de";

    @Test
    void test() {
        EmailAlreadyRegisteredException exception = new EmailAlreadyRegisteredException(EMAIL);

        assertNotNull(exception, "Exception should not be null.");
        assertEquals(EXPECTED_MESSAGE + EMAIL, exception.getMessage(), "Default message should match the expected value.");
    }
}
