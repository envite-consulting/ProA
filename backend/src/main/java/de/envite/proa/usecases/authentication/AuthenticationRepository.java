package de.envite.proa.usecases.authentication;

import de.envite.proa.entities.User;

public interface AuthenticationRepository {

	String login(User user);

	void register(User user);
}
