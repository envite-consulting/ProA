package de.envite.proa.usecases.authentication.exceptions;

public class InvalidPasswordException extends Exception {
    public InvalidPasswordException() {
        super("Invalid password provided.");
    }
}
