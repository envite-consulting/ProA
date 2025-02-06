package de.envite.proa.startup;

import de.envite.proa.entities.authentication.User;
import de.envite.proa.usecases.authentication.AuthenticationUsecase;
import de.envite.proa.usecases.authentication.exceptions.EmailAlreadyRegisteredException;
import de.envite.proa.usecases.user.UserUsecase;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class AdminInitializerTest {

	private static final String ROLE = "Admin";

	@InjectMocks
	private AdminInitializer adminInitializer;

	@Mock
	private UserUsecase userUsecase;

	@Mock
	private AuthenticationUsecase authenticationUsecase;

	@Inject
	@ConfigProperty(name = "admin.email")
	String adminEmail;

	@Inject
	@ConfigProperty(name = "admin.password")
	String adminPassword;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testAdminAlreadyExists() throws EmailAlreadyRegisteredException {
		when(userUsecase.findByEmail(adminEmail)).thenReturn(new User());

		adminInitializer.init();

		verify(userUsecase).findByEmail(adminEmail);
		verify(authenticationUsecase, never()).register(any(User.class));
	}

	@Test
	public void testAdminDoesNotExist() throws EmailAlreadyRegisteredException {
		when(userUsecase.findByEmail(adminEmail)).thenReturn(null);

		adminInitializer.init();

		User user = new User();
		user.setEmail(adminEmail);
		user.setPassword(adminPassword);
		user.setRole(ROLE);

		verify(userUsecase).findByEmail(adminEmail);
		verify(authenticationUsecase, times(1)).register(user);
	}

	@Test
	public void testAdminDoesNotExist_Exception() throws EmailAlreadyRegisteredException {
		User user = new User();
		user.setEmail(adminEmail);
		user.setPassword(adminPassword);
		user.setRole(ROLE);

		when(userUsecase.findByEmail(adminEmail)).thenReturn(null);
		doThrow(new RuntimeException("Test Exception")).when(authenticationUsecase).register(user);

		ByteArrayOutputStream errContent = new ByteArrayOutputStream();
		System.setErr(new PrintStream(errContent));

		adminInitializer.init();

		System.setErr(System.err);

		String output = errContent.toString();
		assertTrue(output.contains("Test Exception"));

		verify(userUsecase).findByEmail(adminEmail);
		verify(authenticationUsecase, times(1)).register(user);
	}
}