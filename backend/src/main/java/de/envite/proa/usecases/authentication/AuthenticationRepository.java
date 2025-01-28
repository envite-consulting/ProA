package de.envite.proa.usecases.authentication;

import de.envite.proa.entities.User;
import de.envite.proa.usecases.authentication.exceptions.EmailAlreadyRegisteredException;
import de.envite.proa.usecases.authentication.exceptions.EmailNotFoundException;
import de.envite.proa.usecases.authentication.exceptions.InvalidPasswordException;
import de.envite.proa.usecases.authentication.exceptions.AccountLockedException;

public interface AuthenticationRepository {

	String login(User user) throws EmailNotFoundException, InvalidPasswordException, AccountLockedException;

	void register(User user) throws EmailAlreadyRegisteredException;
}
