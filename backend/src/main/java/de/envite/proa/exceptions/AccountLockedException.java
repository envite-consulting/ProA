package de.envite.proa.exceptions;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("Account is locked.");
    }
}
