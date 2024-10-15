package de.envite.proa.usecases.authentication;

import de.envite.proa.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AuthenticationUsecase {

	@Inject
	private AuthenticationRepository repository;

	public User login(User user) {
		return repository.login(user);
	}

	public User register(User user) {
		return repository.register(user);
	}

	public User patchUser(Long userId, User user) {
		return repository.patchUser(userId, user);
	}

	public User findByEmail(String email) {
		return repository.findByEmail(email);
	}
}
