package de.envite.proa.usecases.authentication;

import de.envite.proa.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthenticationUsecase {

	@Inject
	AuthenticationRepository repository;

	public String login(User user) {
		return repository.login(user);
	}

	public void register(User user) {
		repository.register(user);
	}
}
