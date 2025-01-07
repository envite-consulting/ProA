package de.envite.proa.rest;

import de.envite.proa.entities.User;
import de.envite.proa.exceptions.AccountLockedException;
import de.envite.proa.exceptions.EmailAlreadyRegisteredException;
import de.envite.proa.exceptions.InvalidPasswordException;
import de.envite.proa.usecases.authentication.AuthenticationUsecase;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AuthenticationResourceTest {

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
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginSuccess() {
        when(authenticationUsecase.login(user)).thenReturn(JWT);

        Response response = authenticationResource.login(user);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(JWT, response.getEntity());

        verify(authenticationUsecase, times(1)).login(user);
    }

    @Test
    public void testLoginInvalidCredentials() {
        doThrow(InvalidPasswordException.class).when(authenticationUsecase).login(user);

        Response response = authenticationResource.login(user);

        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());

        verify(authenticationUsecase, times(1)).login(user);
    }

    @Test
    public void testLoginAccountLocked() {
        doThrow(AccountLockedException.class).when(authenticationUsecase).login(user);

        Response response = authenticationResource.login(user);

        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());

        verify(authenticationUsecase, times(1)).login(user);
    }

    @Test
    public void testLoginInternalError() {
        doThrow(new RuntimeException("Unexpected error")).when(authenticationUsecase).login(user);

        Response response = authenticationResource.login(user);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        verify(authenticationUsecase, times(1)).login(user);
    }

    @Test
    public void testRegisterSuccess() {
        doNothing().when(authenticationUsecase).register(user);

        Response response = authenticationResource.register(user);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        verify(authenticationUsecase, times(1)).register(user);
    }

    @Test
    public void testRegisterEmailAlreadyRegistered() {
        doThrow(EmailAlreadyRegisteredException.class).when(authenticationUsecase).register(user);

        Response response = authenticationResource.register(user);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());

        verify(authenticationUsecase, times(1)).register(user);
    }

    @Test
    public void testRegisterInternalError() {
        doThrow(new RuntimeException("Unexpected error")).when(authenticationUsecase).register(user);

        Response response = authenticationResource.register(user);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        verify(authenticationUsecase, times(1)).register(user);
    }
}
