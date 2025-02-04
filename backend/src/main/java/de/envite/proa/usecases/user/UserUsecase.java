package de.envite.proa.usecases.user;

import de.envite.proa.entities.authentication.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class UserUsecase {

    @Inject
    UserRepository repository;

    public User findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public User patchUser(Long userId, User user) {
        return repository.patchUser(userId, user);
    }

    public User findById(Long id) {
        return repository.findById(id);
    }

    public List<User> getAllUsers() {
        return repository.getAllUsers();
    }

    public void deleteById(Long id) {
    	repository.deleteById(id);
    }
}
