package de.envite.proa.usecases.user;

import de.envite.proa.entities.authentication.User;

import java.util.List;

public interface UserRepository {
    User findByEmail(String email);

    User patchUser(Long userId, User user);

    User findById(Long id);

    List<User> getAllUsers();

    void deleteById(Long id);
}
