package de.envite.proa.repository;

import de.envite.proa.entities.User;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.usecases.authentication.AuthenticationRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;

@ApplicationScoped
public class AuthenticationRepositoryImpl implements AuthenticationRepository {

	private final AuthenticationDao authenticationDao;

	@Inject
	public AuthenticationRepositoryImpl(AuthenticationDao authenticationDao) {
		this.authenticationDao = authenticationDao;
	}

	@Override
	public User login(User user) {
		UserTable userTable = authenticationDao.findByEmail(user.getEmail());
		if (userTable == null) {
			return null;
		}

		boolean doesPasswordMatch = BcryptUtil.matches(user.getPassword(), userTable.getPassword());
		if (!doesPasswordMatch) {
			return null;
		}

		return map(userTable);
	}

	@Override
	public User register(User user) {
		boolean emailAlreadyRegistered = authenticationDao.findByEmail(user.getEmail()) != null;
		if (emailAlreadyRegistered) {
			return null;
		}

		user.setPassword(hashPassword(user.getPassword()));

		UserTable userTable = map(user);
		userTable.setCreatedAt(LocalDateTime.now());
		userTable.setModifiedAt(LocalDateTime.now());
		return map(authenticationDao.register(userTable));
	}

	@Override
	public User patchUser(Long userId, User user) {
		UserTable userTable = authenticationDao.findById(userId);
		mergeIntoUserTableIfNotNull(userTable, user);
		userTable.setModifiedAt(LocalDateTime.now());
		return map(authenticationDao.patchUser(userTable));
	}

	private UserTable map(User user) {
		UserTable table = new UserTable();
		table.setEmail(user.getEmail());
		table.setFirstName(user.getFirstName());
		table.setLastName(user.getLastName());
		table.setPassword(user.getPassword());
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
}
