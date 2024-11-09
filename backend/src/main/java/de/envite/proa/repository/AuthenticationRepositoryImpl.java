package de.envite.proa.repository;

import de.envite.proa.entities.User;
import de.envite.proa.exceptions.EmailAlreadyRegisteredException;
import de.envite.proa.exceptions.EmailNotFoundException;
import de.envite.proa.exceptions.InvalidPasswordException;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.usecases.authentication.AuthenticationRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

@ApplicationScoped
public class AuthenticationRepositoryImpl implements AuthenticationRepository {

	private final AuthenticationDao authenticationDao;
	private final UserDao userDao;

	@Inject
	public AuthenticationRepositoryImpl(AuthenticationDao authenticationDao, UserDao userDao) {
		this.authenticationDao = authenticationDao;
		this.userDao = userDao;
	}

	@Override
	public User login(User user) {
		String email = user.getEmail();

		UserTable userTable = userDao.findByEmail(email);
		if (userTable == null) throw new EmailNotFoundException(email);

		boolean doesPasswordMatch = BcryptUtil.matches(user.getPassword(), userTable.getPassword());
		if (!doesPasswordMatch) throw new InvalidPasswordException();

		return UserMapper.map(userTable);
	}

	@Override
	public User register(User user) {
		String email = user.getEmail();

		boolean emailAlreadyRegistered = userDao.findByEmail(email) != null;
		if (emailAlreadyRegistered) throw new EmailAlreadyRegisteredException(email);

		user.setPassword(hashPassword(user.getPassword()));

		UserTable userTable = UserMapper.map(user);
		userTable.setCreatedAt(LocalDateTime.now());
		userTable.setModifiedAt(LocalDateTime.now());
		return UserMapper.map(authenticationDao.register(userTable));
	}

	private String hashPassword(String plainPassword) {
		return BcryptUtil.bcryptHash(plainPassword);
	}
}
