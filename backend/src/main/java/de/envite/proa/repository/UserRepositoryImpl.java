package de.envite.proa.repository;

import de.envite.proa.entities.User;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.usecases.user.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

@ApplicationScoped
public class UserRepositoryImpl implements UserRepository {

    @Inject
    UserDao userDao;

    @Override
    public User findByEmail(String email) {
        UserTable userTable = userDao.findByEmail(email);
        return userTable == null ? null : UserMapper.map(userTable);
    }

    public User patchUser(Long userId, User user) {
        UserTable userTable = userDao.findById(userId);
        mergeIntoUserTableIfNotNull(userTable, user);
        userTable.setModifiedAt(LocalDateTime.now());
        return UserMapper.map(userDao.patchUser(userTable));
    }

    private void mergeIntoUserTableIfNotNull(UserTable userTable, User user) {
        userTable.setEmail(user.getEmail() != null ? user.getEmail() : userTable.getEmail());
        userTable.setFirstName(user.getFirstName() != null ? user.getFirstName() : userTable.getFirstName());
        userTable.setLastName(user.getLastName( ) != null ? user.getLastName() : userTable.getLastName());
        userTable.setPassword(user.getPassword() != null ? hashPassword(user.getPassword()) : userTable.getPassword());
    }

    private String hashPassword(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }
}
