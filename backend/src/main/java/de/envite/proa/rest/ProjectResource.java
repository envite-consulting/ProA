package de.envite.proa.rest;

import de.envite.proa.entities.project.Project;
import de.envite.proa.entities.project.ProjectVersion;
import de.envite.proa.security.RolesAllowedIfWebVersion;
import de.envite.proa.usecases.project.ProjectUsecase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.Map;

@Path("/api")
public class ProjectResource {

	private static final String USER_ID = "userId";

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
	 * @param name
	 * 		the name of the project to be created
	 * @return the created project
	 */
	@POST
	@Path("/project")
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public Response createProject(@RestForm String name, @RestForm String version) {
		if (appMode.equals("web")) {
			Long userId = Long.parseLong(jwt.getClaim(USER_ID).toString());
			Project project = usecase.createProject(userId, name, version);
			return Response.status(Response.Status.CREATED)
					.entity(project)
					.build();
		}

		Project project = usecase.createProject(name, version);
		return Response.status(Response.Status.CREATED)
				.entity(project)
				.build();
	}

	/**
	 * This method gets the names and the corresponding ids of all projects in order to show them as tiles in the
	 * frontend
	 */
	@GET
	@Path("/project")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public List<Project> getProjects() {
		if (appMode.equals("web")) {
			Long userId = Long.parseLong(jwt.getClaim(USER_ID).toString());
			return usecase.getProjects(userId);
		}
		return usecase.getProjects();
	}

	@GET
	@Path("/project/{projectId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public Response getProject(@RestPath Long projectId) {
		if (appMode.equals("web")) {
			Long userId = Long.parseLong(jwt.getClaim(USER_ID).toString());
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

	@POST
	@Path("/project/{projectId}")
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public Response addVersion(@RestPath Long projectId, @RestForm String versionName) {
		if (appMode.equals("web")) {
			Long userId = Long.parseLong(jwt.getClaim(USER_ID).toString());
			ProjectVersion projectVersion = usecase.addVersion(userId, projectId, versionName);
			return Response.status(Response.Status.CREATED)
					.entity(projectVersion)
					.build();
		}

		ProjectVersion projectVersion = usecase.addVersion(projectId, versionName);
		return Response.status(Response.Status.CREATED)
				.entity(projectVersion)
				.build();
	}

	@DELETE
	@Path("/project/{projectId}/{versionId}")
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public void removeVersion(@RestPath Long projectId, @RestPath Long versionId) {
		if (appMode.equals("web")) {
			Long userId = Long.parseLong(jwt.getClaim(USER_ID).toString());
			usecase.removeVersion(userId, projectId, versionId);
			return;
		}
		usecase.removeVersion(projectId, versionId);
	}

	@POST
	@Path("/project/{projectId}/contributor")
	@RolesAllowed({ "User", "Admin" })
	public Response addContributor(@RestPath Long projectId, @RestForm String email) {
		try {
			Long userId = Long.parseLong(jwt.getClaim(USER_ID).toString());
			usecase.addContributor(userId, projectId, email);
			return Response.ok()
					.entity(Map.of("message", "Contributor added successfully"))
					.build();
		} catch (EntityNotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity(Map.of("error", "Project or user not found"))
					.build();
		} catch (ForbiddenException e) {
			return Response.status(Response.Status.FORBIDDEN)
					.entity(Map.of("error", "You don't have permission to add contributors"))
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Map.of("error", "Failed to add contributor"))
					.build();
		}
	}

	@DELETE
	@Path("/project/{projectId}/contributor/{contributorId}")
	@RolesAllowed({ "User", "Admin" })
	public Response removeContributor(@RestPath Long projectId, @RestPath Long contributorId) {
		try {
			Long userId = Long.parseLong(jwt.getClaim(USER_ID).toString());
			usecase.removeContributor(userId, projectId, contributorId);
			return Response.noContent().build();
		} catch (EntityNotFoundException e) {
			return Response.status(Response.Status.NOT_FOUND)
					.entity(Map.of("error", "Project not found"))
					.build();
		} catch (ForbiddenException e) {
			return Response.status(Response.Status.FORBIDDEN)
					.entity(Map.of("error", "You don't have permission to remove contributors"))
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(Map.of("error", "Failed to remove contributor"))
					.build();
		}
	}
}
