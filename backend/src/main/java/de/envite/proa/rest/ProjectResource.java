package de.envite.proa.rest;

import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import de.envite.proa.entities.Project;
import de.envite.proa.usecases.project.ProjectUsecase;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("")
public class ProjectResource {

	@Inject
	private ProjectUsecase usecase;

	/**
	 * Creates a new project
	 *
	 * @param name the name of the project to be created
	 * @return the created project
	 */
	@POST
	@Path("/project")
	public Project uploadProcessModel(@RestForm String name, @RestForm String version) {

		return usecase.createProject(name, version);
	}

	/**
	 * This methods gets the names and the corresponding ids of all projects in
	 * order to show them as tiles in the frontend
	 */
	@GET
	@Path("/project")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Project> getProcessInformation() {
		return usecase.getProjects();
	}

	@GET
	@Path("/project/{projectId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Project getProcessInformation(@RestPath Long projectId) {
		return usecase.getProject(projectId);
	}
}