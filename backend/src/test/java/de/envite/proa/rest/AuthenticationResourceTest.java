package de.envite.proa.rest;

import de.envite.proa.entities.authentication.User;
import de.envite.proa.usecases.authentication.AuthenticationUsecase;
import de.envite.proa.usecases.authentication.exceptions.AccountLockedException;
import de.envite.proa.usecases.authentication.exceptions.EmailAlreadyRegisteredException;
import de.envite.proa.usecases.authentication.exceptions.EmailNotFoundException;
import de.envite.proa.usecases.authentication.exceptions.InvalidPasswordException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthenticationResourceTest {

	private static final String EMAIL = "test@example.com";
	private static final String PASSWORD = "password123";
	private static final String JWT = "jwt-token";

	private static User user;

	@InjectMocks
	private AuthenticationResource authenticationResource;

	@Mock
	private AuthenticationUsecase authenticationUsecase;

	@BeforeAll
	public static void setUpClass() {
		user = new User();
		user.setEmail(EMAIL);
		user.setPassword(PASSWORD);
	}

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testLoginSuccess() throws EmailNotFoundException, AccountLockedException, InvalidPasswordException {
		when(authenticationUsecase.login(user)).thenReturn(JWT);

		Response response = authenticationResource.login(user);

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
		assertEquals(JWT, response.getEntity());

		verify(authenticationUsecase, times(1)).login(user);
	}

	@Test
	void testLoginInvalidCredentials()
			throws EmailNotFoundException, AccountLockedException, InvalidPasswordException {
		doThrow(InvalidPasswordException.class).when(authenticationUsecase).login(user);

		Response response = authenticationResource.login(user);

		assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());

		verify(authenticationUsecase, times(1)).login(user);
	}

	@Test
	void testLoginAccountLocked()
			throws EmailNotFoundException, AccountLockedException, InvalidPasswordException {
		doThrow(AccountLockedException.class).when(authenticationUsecase).login(user);

		Response response = authenticationResource.login(user);

		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());

		verify(authenticationUsecase, times(1)).login(user);
	}

	@Test
	void testLoginInternalError()
			throws EmailNotFoundException, AccountLockedException, InvalidPasswordException {
		doThrow(new RuntimeException("Unexpected error")).when(authenticationUsecase).login(user);

		Response response = authenticationResource.login(user);

		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

		verify(authenticationUsecase, times(1)).login(user);
	}

	@Test
	void testRegisterSuccess() throws EmailAlreadyRegisteredException {
		doNothing().when(authenticationUsecase).register(user);

		Response response = authenticationResource.register(user);

		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

		verify(authenticationUsecase, times(1)).register(user);
	}

	@Test
	void testRegisterEmailAlreadyRegistered() throws EmailAlreadyRegisteredException {
		doThrow(EmailAlreadyRegisteredException.class).when(authenticationUsecase).register(user);

		Response response = authenticationResource.register(user);

		assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());

		verify(authenticationUsecase, times(1)).register(user);
	}

	@Test
	void testRegisterInternalError() throws EmailAlreadyRegisteredException {
		doThrow(new RuntimeException("Unexpected error")).when(authenticationUsecase).register(user);

		Response response = authenticationResource.register(user);

		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

		verify(authenticationUsecase, times(1)).register(user);
	}
}
