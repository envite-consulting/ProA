package de.envite.proa.repository;

import de.envite.proa.entities.Role;
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

		return map(userTable);
	}

	@Override
	public User register(User user) {
		String email = user.getEmail();

		boolean emailAlreadyRegistered = userDao.findByEmail(email) != null;
		if (emailAlreadyRegistered) throw new EmailAlreadyRegisteredException(email);

		user.setPassword(hashPassword(user.getPassword()));

		UserTable userTable = map(user);
		userTable.setCreatedAt(LocalDateTime.now());
		userTable.setModifiedAt(LocalDateTime.now());
		return map(authenticationDao.register(userTable));
	}

	@Override
	public User patchUser(Long userId, User user) {
		UserTable userTable = userDao.findById(userId);
		mergeIntoUserTableIfNotNull(userTable, user);
		userTable.setModifiedAt(LocalDateTime.now());
		return map(userDao.patchUser(userTable));
	}

	private UserTable map(User user) {
		UserTable table = new UserTable();
		table.setEmail(user.getEmail());
		table.setFirstName(user.getFirstName());
		table.setLastName(user.getLastName());
		table.setPassword(user.getPassword());
		switch (user.getRole()) {
			case "Admin":
				table.setRole(Role.Admin);
				break;
			case "User":
				table.setRole(Role.User);
				break;
			default:
				throw new IllegalArgumentException("Unknown role: " + user.getRole());
		}
		return table;
	}

	private User map(UserTable table) {
		User user = new User();
		user.setId(table.getId());
		user.setEmail(table.getEmail());
		user.setFirstName(table.getFirstName());
		user.setLastName(table.getLastName());
		user.setPassword(table.getPassword());
		user.setCreatedAt(table.getCreatedAt());
		user.setModifiedAt(table.getModifiedAt());
		switch (table.getRole()) {
			case Admin:
				user.setRole("Admin");
				break;
			case User:
				user.setRole("User");
				break;
		}
		return user;
	}

	private String hashPassword(String plainPassword) {
		return BcryptUtil.bcryptHash(plainPassword);
	}

	private void mergeIntoUserTableIfNotNull(UserTable userTable, User user) {
		userTable.setEmail(user.getEmail() != null ? user.getEmail() : userTable.getEmail());
		userTable.setFirstName(user.getFirstName() != null ? user.getFirstName() : userTable.getFirstName());
		userTable.setLastName(user.getLastName( ) != null ? user.getLastName() : userTable.getLastName());
		userTable.setPassword(user.getPassword() != null ? hashPassword(user.getPassword()) : userTable.getPassword());
	}

	@Override
	public User findByEmail(String email) {
		UserTable userTable = userDao.findByEmail(email);
		if (userTable == null) {
			return null;
		}
		return map(userDao.findByEmail(email));
	}
}
