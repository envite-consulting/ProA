package de.envite.proa.usecases.authentication;

import de.envite.proa.entities.authentication.User;
import de.envite.proa.usecases.authentication.exceptions.AccountLockedException;
import de.envite.proa.usecases.authentication.exceptions.EmailAlreadyRegisteredException;
import de.envite.proa.usecases.authentication.exceptions.EmailNotFoundException;
import de.envite.proa.usecases.authentication.exceptions.InvalidPasswordException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthenticationUsecaseTest {

	private static User user;
	private static final String EMAIL = "test@test.de";
	private static final String PASSWORD = "password123";
	private static final String JWT = "jwt-token";

	@Mock
	private AuthenticationRepository repository;

	@InjectMocks
	private AuthenticationUsecase usecase;

	@BeforeAll
	static void beforeAll() {
		user = new User();
		user.setEmail(EMAIL);
		user.setPassword(PASSWORD);
	}

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testLogin() throws EmailNotFoundException, AccountLockedException, InvalidPasswordException {
		when(repository.login(user)).thenReturn(JWT);

		String actualToken = usecase.login(user);

		assertEquals(JWT, actualToken, "Login should return the correct token.");
		verify(repository, times(1)).login(user);
	}

	@Test
	void testRegister() throws EmailAlreadyRegisteredException {
		usecase.register(user);

		verify(repository, times(1)).register(user);
	}
}
