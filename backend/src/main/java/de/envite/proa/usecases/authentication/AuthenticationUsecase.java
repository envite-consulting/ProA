package de.envite.proa.usecases.authentication;

import de.envite.proa.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthenticationUsecase {

	@Inject
	AuthenticationRepository repository;

	public User login(User user) {
		return repository.login(user);
	}

	public User register(User user) {
		return repository.register(user);
	}
}
