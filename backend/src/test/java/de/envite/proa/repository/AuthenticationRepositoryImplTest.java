package de.envite.proa.repository;

import de.envite.proa.authservice.TokenService;
import de.envite.proa.entities.Role;
import de.envite.proa.entities.User;
import de.envite.proa.exceptions.AccountLockedException;
import de.envite.proa.exceptions.EmailAlreadyRegisteredException;
import de.envite.proa.exceptions.EmailNotFoundException;
import de.envite.proa.exceptions.InvalidPasswordException;
import de.envite.proa.repository.tables.UserTable;
import io.quarkus.elytron.security.common.BcryptUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationRepositoryImplTest {

	private static final String USER_EMAIL = "test@example.com";
	private static final String USER_PASSWORD = "password";
	private static final String USER_ROLE = "User";
	private static final String HASHED_PASSWORD = BcryptUtil.bcryptHash(USER_PASSWORD);
	private static final String OTHER_HASHED_PASSWORD = BcryptUtil.bcryptHash("otherPassword");
	private static final String TOKEN = "mockedToken";

	@InjectMocks
	private AuthenticationRepositoryImpl authenticationRepository;

	@Mock
	private AuthenticationDao authenticationDao;

	@Mock
	private UserDao userDao;

	@Mock
	private TokenService tokenService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testLogin_SuccessfulLogin() {
		User user = new User();
		user.setEmail(USER_EMAIL);
		user.setPassword(USER_PASSWORD);
		user.setRole(USER_ROLE);

		UserTable userTable = new UserTable();
		userTable.setEmail(USER_EMAIL);
		userTable.setPassword(HASHED_PASSWORD);
		userTable.setRole(Role.valueOf(USER_ROLE));

		when(userDao.findByEmail(USER_EMAIL)).thenReturn(userTable);

		User userArgument = new User();
		userArgument.setEmail(USER_EMAIL);
		userArgument.setRole(USER_ROLE);
		when(tokenService.generateToken(userArgument, USER_ROLE)).thenReturn(TOKEN);

		String token = authenticationRepository.login(user);

		assertNotNull(token);
		assertEquals(TOKEN, token);
		verify(userDao).findByEmail(USER_EMAIL);
		verify(tokenService).generateToken(userArgument, USER_ROLE);
		verify(userDao).patchUser(userTable);
	}

	@Test
	void testLogin_EmailNotFound() {
		User user = new User();
		user.setEmail(USER_EMAIL);
		user.setPassword(USER_PASSWORD);

		when(userDao.findByEmail(USER_EMAIL)).thenReturn(null);

		assertThrows(EmailNotFoundException.class, () -> authenticationRepository.login(user));
		verify(userDao).findByEmail(USER_EMAIL);
	}

	@Test
	void testLogin_InvalidPassword() {
		User user = new User();
		user.setEmail(USER_EMAIL);
		user.setPassword(USER_PASSWORD);

		UserTable userTable = new UserTable();
		userTable.setEmail(USER_EMAIL);
		userTable.setPassword(OTHER_HASHED_PASSWORD);

		when(userDao.findByEmail(USER_EMAIL)).thenReturn(userTable);

		assertThrows(InvalidPasswordException.class, () -> authenticationRepository.login(user));
		verify(userDao).findByEmail(USER_EMAIL);
		verify(userDao).patchUser(userTable);
	}

	@Test
	void testLogin_AccountLocked() {
		User user = new User();
		user.setEmail(USER_EMAIL);
		user.setPassword(USER_PASSWORD);

		UserTable userTable = new UserTable();
		userTable.setEmail(USER_EMAIL);
		userTable.setPassword(HASHED_PASSWORD);
		userTable.setFailedLoginAttempts(3);

		when(userDao.findByEmail(USER_EMAIL)).thenReturn(userTable);

		assertThrows(AccountLockedException.class, () -> authenticationRepository.login(user));
		verify(userDao).findByEmail(USER_EMAIL);
	}

	@Test
	void testLogin_InvalidPassword_AccountLocked() {
		User user = new User();
		user.setEmail(USER_EMAIL);
		user.setPassword(USER_PASSWORD);

		UserTable userTable = new UserTable();
		userTable.setEmail(USER_EMAIL);
		userTable.setPassword(OTHER_HASHED_PASSWORD);
		userTable.setFailedLoginAttempts(2);

		when(userDao.findByEmail(USER_EMAIL)).thenReturn(userTable);

		assertThrows(AccountLockedException.class, () -> authenticationRepository.login(user));
		verify(userDao).findByEmail(USER_EMAIL);
	}

	@Test
	void testRegister_SuccessfulRegistration() {
		User user = new User();
		user.setEmail(USER_EMAIL);
		user.setPassword(USER_PASSWORD);
		user.setRole(USER_ROLE);

		when(userDao.findByEmail(USER_EMAIL)).thenReturn(null);
		doNothing().when(authenticationDao).register(any());

		authenticationRepository.register(user);

		verify(userDao).findByEmail(USER_EMAIL);
		verify(authenticationDao).register(any());
	}

	@Test
	void testRegister_EmailAlreadyRegistered() {
		User user = new User();
		user.setEmail(USER_EMAIL);

		UserTable existingUser = new UserTable();
		existingUser.setEmail(USER_EMAIL);

		when(userDao.findByEmail(USER_EMAIL)).thenReturn(existingUser);

		assertThrows(EmailAlreadyRegisteredException.class, () -> authenticationRepository.register(user));
		verify(userDao).findByEmail(USER_EMAIL);
		verifyNoInteractions(authenticationDao);
	}
}
