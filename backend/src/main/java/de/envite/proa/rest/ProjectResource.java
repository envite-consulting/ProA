package de.envite.proa.rest;

import java.util.List;

import de.envite.proa.security.RolesAllowedIfWebVersion;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import de.envite.proa.entities.Project;
import de.envite.proa.usecases.project.ProjectUsecase;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public class ProjectResource {

    @Inject
    private ProjectUsecase usecase;

    @Inject
    JsonWebToken jwt;

    @Inject
    @ConfigProperty(name = "app.mode", defaultValue = "desktop")
    String appMode;

    /**
     * Creates a new project
     *
     * @param name the name of the project to be created
     * @return the created project
     */
    @POST
    @Path("/project")
    @RolesAllowedIfWebVersion({"User", "Admin"})
    public Project createProject(@RestForm String name, @RestForm String version) {
        if (appMode.equals("web")) {
            Long userId = Long.parseLong(jwt.getClaim("userId").toString());
            return usecase.createProject(userId, name, version);
        }
        return usecase.createProject(name, version);
    }

    /**
     * This methods gets the names and the corresponding ids of all projects in
     * order to show them as tiles in the frontend
     */
    @GET
    @Path("/project")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowedIfWebVersion({"User", "Admin"})
    public List<Project> getProjects() {
        if (appMode.equals("web")) {
            Long userId = Long.parseLong(jwt.getClaim("userId").toString());
            return usecase.getProjects(userId);
        }
        return usecase.getProjects();
    }

    @GET
    @Path("/project/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowedIfWebVersion({"User", "Admin"})
    public Response getProject(@RestPath Long projectId) {
        if (appMode.equals("web")) {
            Long userId = Long.parseLong(jwt.getClaim("userId").toString());
            try {
                return Response.ok().entity(usecase.getProject(userId, projectId)).build();
            } catch (NotFoundException e) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } catch (ForbiddenException e) {
                return Response.status(Response.Status.FORBIDDEN).build();
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        try {
            return Response.ok().entity(usecase.getProject(projectId)).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}