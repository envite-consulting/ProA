package de.envite.proa.usecases.authentication.exceptions;

public class EmailNotFoundException extends Exception {
    public EmailNotFoundException(String email) {
        super("Email not found: " + email);
    }
}
