package de.envite.proa.rest;

import de.envite.proa.entities.User;
import de.envite.proa.exceptions.AccountLockedException;
import de.envite.proa.exceptions.EmailAlreadyRegisteredException;
import de.envite.proa.exceptions.EmailNotFoundException;
import de.envite.proa.exceptions.InvalidPasswordException;
import de.envite.proa.usecases.authentication.AuthenticationUsecase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api/authentication")
public class AuthenticationResource {

	@Inject
	AuthenticationUsecase usecase;

	@POST
	@Path("/login")
	public Response login(User user) {
		try {
			String token = usecase.login(user);
			return Response.ok().entity(token).build();
		} catch (EmailNotFoundException | InvalidPasswordException e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		} catch (AccountLockedException e) {
			return Response.status(Response.Status.FORBIDDEN).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("/register")
	@RolesAllowed({"Admin"})
	public Response register(User user) {
		try {
			usecase.register(user);
			return Response.status(Response.Status.CREATED).build();
		} catch (EmailAlreadyRegisteredException e) {
			return Response.status(Response.Status.CONFLICT).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
