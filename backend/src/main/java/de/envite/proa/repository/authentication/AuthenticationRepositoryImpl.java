package de.envite.proa.repository.authentication;

import de.envite.proa.usecases.authentication.exceptions.AccountLockedException;
import de.envite.proa.usecases.authentication.exceptions.EmailAlreadyRegisteredException;
import de.envite.proa.usecases.authentication.exceptions.EmailNotFoundException;
import de.envite.proa.usecases.authentication.exceptions.InvalidPasswordException;
import de.envite.proa.entities.authentication.User;
import de.envite.proa.repository.user.UserDao;
import de.envite.proa.repository.user.UserMapper;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.authservice.TokenService;
import de.envite.proa.usecases.authentication.AuthenticationRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;

@ApplicationScoped
public class AuthenticationRepositoryImpl implements AuthenticationRepository {

	private final AuthenticationDao authenticationDao;
	private final UserDao userDao;
	private final TokenService tokenService;

	@Inject
	public AuthenticationRepositoryImpl(AuthenticationDao authenticationDao, UserDao userDao, TokenService tokenService) {
		this.authenticationDao = authenticationDao;
		this.userDao = userDao;
		this.tokenService = tokenService;
	}

	@Override
	public String login(User user) throws EmailNotFoundException, InvalidPasswordException, AccountLockedException {
		String email = user.getEmail();

		UserTable userTable = userDao.findByEmail(email);
		if (userTable == null) {
			throw new EmailNotFoundException(email);
		}

		if (userTable.getFailedLoginAttempts() == 3) {
			throw new AccountLockedException();
		}

		boolean doesPasswordMatch = BcryptUtil.matches(user.getPassword(), userTable.getPassword());
		if (!doesPasswordMatch) {
			userTable.setFailedLoginAttempts(userTable.getFailedLoginAttempts() + 1);
			userDao.patchUser(userTable);
			if (userTable.getFailedLoginAttempts() == 3) {
				throw new AccountLockedException();
			}
			throw new InvalidPasswordException();
		}

		userTable.setFailedLoginAttempts(0);
		userDao.patchUser(userTable);

		User loggedInUser = UserMapper.map(userTable);
		return tokenService.generateToken(loggedInUser, loggedInUser.getRole());
	}

	@Override
	public void register(User user) throws EmailAlreadyRegisteredException {
		String email = user.getEmail();

		boolean emailAlreadyRegistered = userDao.findByEmail(email) != null;
		if (emailAlreadyRegistered) {
			throw new EmailAlreadyRegisteredException(email);
		}

		user.setPassword(hashPassword(user.getPassword()));
		LocalDateTime now = LocalDateTime.now();
		user.setCreatedAt(now);
		user.setModifiedAt(now);

		authenticationDao.register(UserMapper.map(user));
	}

	private String hashPassword(String plainPassword) {
		return BcryptUtil.bcryptHash(plainPassword);
	}
}
