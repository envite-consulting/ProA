package de.envite.proa.rest;

import de.envite.proa.entities.User;
import de.envite.proa.usecases.user.UserUsecase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;

@Path("/api/user")
public class UserResource {

	@Inject
	UserUsecase usecase;

	@Inject
	JsonWebToken jwt;

	@PATCH
	@Path("/{id}")
	@RolesAllowed({"Admin"})
	public Response patchUser(@RestPath Long id, User user) {

		User patchedUser = usecase.patchUser(id, user);
		return Response.ok().entity(patchedUser).build();
	}

	@PATCH
	@Path("")
	@RolesAllowed({"User", "Admin"})
	public Response patchUser(User user) {

		Long id = Long.parseLong(jwt.getClaim("userId").toString());

		User patchedUser = usecase.patchUser(id, user);
		return Response.ok().entity(patchedUser).build();
	}

	@GET
	@Path("")
	@RolesAllowed({"User", "Admin"})
	public Response getUser() {
		Long id = Long.parseLong(jwt.getClaim("userId").toString());
		try {
			User user = usecase.findById(id);
			return Response.ok().entity(user).build();
		} catch (NotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/all")
	@RolesAllowed({"Admin"})
	public Response getUsers() {
		try {
			List<User> users = usecase.getAllUsers();
			return Response.ok().entity(users).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DELETE
	@Path("/{id}")
	@RolesAllowed({"Admin"})
	public Response deleteById(@RestPath Long id) {
		try {
			usecase.deleteById(id);
			return Response.ok().build();
		} catch (NoResultException e) {
			return Response.status(Response.Status.NOT_FOUND).build();
		} catch (Exception e) {return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
