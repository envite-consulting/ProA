package de.envite.proa.rest;

import de.envite.proa.entities.User;
import de.envite.proa.usecases.user.UserUsecase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestPath;

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
}
