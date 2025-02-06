package de.envite.proa.usecases.authentication.exceptions;

public class AccountLockedException extends Exception {
    public AccountLockedException() {
        super("Account is locked.");
    }
}
