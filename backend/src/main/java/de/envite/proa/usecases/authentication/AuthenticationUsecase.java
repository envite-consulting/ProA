package de.envite.proa.usecases.authentication;

import de.envite.proa.entities.User;
import de.envite.proa.usecases.authentication.exceptions.AccountLockedException;
import de.envite.proa.usecases.authentication.exceptions.EmailAlreadyRegisteredException;
import de.envite.proa.usecases.authentication.exceptions.EmailNotFoundException;
import de.envite.proa.usecases.authentication.exceptions.InvalidPasswordException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthenticationUsecase {

	@Inject
	AuthenticationRepository repository;

	public String login(User user) throws EmailNotFoundException, InvalidPasswordException, AccountLockedException {
		return repository.login(user);
	}

	public void register(User user) throws EmailAlreadyRegisteredException {
		repository.register(user);
	}
}
