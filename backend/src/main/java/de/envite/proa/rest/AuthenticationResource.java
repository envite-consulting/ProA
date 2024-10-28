package de.envite.proa.rest;

import de.envite.proa.entities.User;
import de.envite.proa.service.TokenService;
import de.envite.proa.usecases.authentication.AuthenticationUsecase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestPath;

import java.net.URI;

@Path("/api/authentication")
public class AuthenticationResource {

	@Inject
	private AuthenticationUsecase usecase;

	@Inject
	TokenService tokenService;

	@Inject
	JsonWebToken jwt;

	@Inject
	SecurityContext securityContext;

	@POST
	@Path("/login")
	public Response login(User user) {
		User loggedInUser = usecase.login(user);
		if (loggedInUser == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		String token = tokenService.generateToken(loggedInUser, loggedInUser.getRole());
		loggedInUser.setToken(token);
		return Response.ok().entity(loggedInUser).build();
	}

	@POST
	@Path("/register")
	@RolesAllowed({"Admin"})
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
	@RolesAllowed({"User", "Admin"})
	public Response patchUser(@RestPath Long id, User user) {

		if (!securityContext.isUserInRole("Admin") && !id.toString().equals(jwt.getClaim("userId").toString())) {
			throw new ForbiddenException("You can only patch your own profile.");
		}

		User patchedUser = usecase.patchUser(id, user);
		return Response.ok().entity(patchedUser).build();
	}
}
