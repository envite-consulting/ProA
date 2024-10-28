package de.envite.proa.rest;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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
    public Project createProject(@RestForm String name, @RestForm String version) {
        if (appMode.equals("web")) {
            throw new ForbiddenException("Not allowed in web mode");
        }
        return usecase.createProject(name, version);
    }

    @POST
    @Path("/project/{userId}")
    @RolesAllowed({"User", "Admin"})
    public Project createProject(@RestPath Long userId, @RestForm String name, @RestForm String version) {
        return usecase.createProject(userId, name, version);
    }

    /**
     * This methods gets the names and the corresponding ids of all projects in
     * order to show them as tiles in the frontend
     */
    @GET
    @Path("/project")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Project> getProjects() {
        if (appMode.equals("web")) {
            throw new ForbiddenException("Not allowed in web mode");
        }
        return usecase.getProjects();
    }

    @GET
    @Path("/project/{userId}")
    @RolesAllowed({"User", "Admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public List<Project> getProjects(@RestPath Long userId) {
        return usecase.getProjects(userId);
    }

    @GET
    @Path("/project/project-id/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Project getProject(@RestPath Long projectId) {
        if (appMode.equals("web")) {
            throw new ForbiddenException("Not allowed in web mode");
        }
        return usecase.getProject(projectId);
    }

    @GET
    @Path("/project/{userId}/{projectId}")
    @RolesAllowed({"User", "Admin"})
    @Produces(MediaType.APPLICATION_JSON)
    public Project getProject(@RestPath Long userId, @RestPath Long projectId) {
        return usecase.getProject(userId, projectId);
    }
}