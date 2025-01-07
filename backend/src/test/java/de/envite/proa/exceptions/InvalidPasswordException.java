package de.envite.proa.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidPasswordExceptionTest {

    private static final String EXPECTED_MESSAGE = "Invalid password provided.";

    @Test
    void test() {
        InvalidPasswordException exception = new InvalidPasswordException();

        assertNotNull(exception, "Exception should not be null.");
        assertEquals(EXPECTED_MESSAGE, exception.getMessage(), "Default message should match the expected value.");
    }
}
