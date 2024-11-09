package de.envite.proa.usecases.user;

import de.envite.proa.entities.User;

public interface UserRepository {
    User findByEmail(String email);

    User patchUser(Long userId, User user);
}
