package de.envite.proa.usecases.authentication;

import de.envite.proa.entities.User;

public interface AuthenticationRepository {

	User login(User user);

	User register(User user);

	User patchUser(Long userId, User user);
}
