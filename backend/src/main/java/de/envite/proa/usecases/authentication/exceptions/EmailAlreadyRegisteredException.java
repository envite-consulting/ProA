package de.envite.proa.usecases.authentication.exceptions;

public class EmailAlreadyRegisteredException extends Exception {
    public EmailAlreadyRegisteredException(String email) {
        super("Email already registered: " + email);
    }
}
