package de.envite.proa.rest;

import org.jboss.resteasy.reactive.RestPath;

import de.envite.proa.entities.ProcessMap;
import de.envite.proa.usecases.processmap.ProcessMapUsecase;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("")
public class ProcessMapResource {

	@Inject
	private ProcessMapUsecase usecase;

	@GET
	@Path("/project/{projectId}/process-map")
	@Produces(MediaType.APPLICATION_JSON)
	public ProcessMap getProcessMap(@RestPath Long projectId) {
		return usecase.getProcessMap(projectId);
	}
}
