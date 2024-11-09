package de.envite.proa.rest;

import de.envite.proa.entities.User;
import de.envite.proa.exceptions.EmailAlreadyRegisteredException;
import de.envite.proa.exceptions.EmailNotFoundException;
import de.envite.proa.exceptions.InvalidPasswordException;
import de.envite.proa.service.TokenService;
import de.envite.proa.usecases.authentication.AuthenticationUsecase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.net.URI;

@Path("/api/authentication")
public class AuthenticationResource {

	@Inject
	AuthenticationUsecase usecase;

	@Inject
	TokenService tokenService;

	@POST
	@Path("/login")
	public Response login(User user) {
		User loggedInUser;

		try {
			loggedInUser = usecase.login(user);
		} catch (EmailNotFoundException | InvalidPasswordException e) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		String token = tokenService.generateToken(loggedInUser, loggedInUser.getRole());
		loggedInUser.setToken(token);
		return Response.ok().entity(loggedInUser).build();
	}

	@POST
	@Path("/register")
	@RolesAllowed({"Admin"})
	public Response register(User user) {
		User registeredUser;

		try {
			registeredUser = usecase.register(user);
		} catch (EmailAlreadyRegisteredException e) {
			return Response.status(Response.Status.CONFLICT).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		URI location = UriBuilder.fromResource(AuthenticationResource.class).path("/{id}")
				.resolveTemplate("id", registeredUser.getId()).build();

		return Response.created(location).entity(registeredUser).build();
	}
}
