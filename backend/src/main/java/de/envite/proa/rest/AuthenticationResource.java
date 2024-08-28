package de.envite.proa.rest;

import de.envite.proa.entities.User;
import de.envite.proa.service.TokenService;
import de.envite.proa.usecases.authentication.AuthenticationUsecase;
import jakarta.inject.Inject;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.resteasy.reactive.RestPath;

import java.net.URI;

@Path("/authentication")
public class AuthenticationResource {

	@Inject
	private AuthenticationUsecase usecase;

	@Inject
	TokenService tokenService;

	@POST
	@Path("/login")
	public Response login(User user) {
		User loggedInUser = usecase.login(user);
		if (loggedInUser == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		String token = tokenService.generateToken(loggedInUser);
		loggedInUser.setToken(token);
		return Response.ok().entity(loggedInUser).build();
	}

	@POST
	@Path("/register")
	public Response register(User user) {
		User registeredUser = usecase.register(user);
		if (registeredUser == null) {
			return Response.status(Response.Status.CONFLICT).build();
		}

		URI location = UriBuilder.fromResource(AuthenticationResource.class).path("/{id}")
				.resolveTemplate("id", registeredUser.getId()).build();

		return Response.created(location).entity(registeredUser).build();
	}

	@PATCH
	@Path("/user/{id}")
	public Response patchUser(@RestPath Long id, User user) {
		User patchedUser = usecase.patchUser(id, user);
		return Response.ok().entity(patchedUser).build();
	}
}
