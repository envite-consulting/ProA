package de.envite.proa.rest;

import de.envite.proa.entities.User;
import de.envite.proa.usecases.user.UserUsecase;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserResourceTest {

    @InjectMocks
    private UserResource resource;

    @Mock
    private UserUsecase usecase;

    @Mock
    private JsonWebToken jwt;

    private static final Long USER_ID = 1L;
    private static final User USER = new User();
    private static final User PATCHED_USER = new User();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPatchUserAdminRole() {
        when(usecase.patchUser(USER_ID, USER)).thenReturn(PATCHED_USER);

        Response response = resource.patchUser(USER_ID, USER);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(PATCHED_USER, response.getEntity());
        verify(usecase, times(1)).patchUser(USER_ID, USER);
    }

    @Test
    public void testPatchUserUserRole() {
        when(jwt.getClaim("userId")).thenReturn(USER_ID.toString());
        when(usecase.patchUser(USER_ID, USER)).thenReturn(PATCHED_USER);

        Response response = resource.patchUser(USER);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(PATCHED_USER, response.getEntity());
        verify(jwt, times(1)).getClaim("userId");
        verify(usecase, times(1)).patchUser(USER_ID, USER);
    }

    @Test
    public void testGetUserSuccess() {
        when(jwt.getClaim("userId")).thenReturn(USER_ID.toString());
        when(usecase.findById(USER_ID)).thenReturn(USER);

        Response response = resource.getUser();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(USER, response.getEntity());
        verify(jwt, times(1)).getClaim("userId");
        verify(usecase, times(1)).findById(USER_ID);
    }

    @Test
    public void testGetUserNotFound() {
        when(jwt.getClaim("userId")).thenReturn(USER_ID.toString());
        when(usecase.findById(USER_ID)).thenThrow(new jakarta.ws.rs.NotFoundException());

        Response response = resource.getUser();

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        verify(jwt, times(1)).getClaim("userId");
        verify(usecase, times(1)).findById(USER_ID);
    }

    @Test
    public void testGetUserServerError() {
        when(jwt.getClaim("userId")).thenReturn(USER_ID.toString());
        when(usecase.findById(USER_ID)).thenThrow(new RuntimeException("Internal Error"));

        Response response = resource.getUser();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(jwt, times(1)).getClaim("userId");
        verify(usecase, times(1)).findById(USER_ID);
    }
}
