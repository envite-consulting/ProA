package de.envite.process.map.rest;

import de.envite.process.map.entities.ProcessMap;
import de.envite.process.map.usecases.processmap.ProcessMapUsecase;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/process-map")
public class ProcessMapResource {

	@Inject
	private ProcessMapUsecase usecase;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ProcessMap getProcessMap() {
		return usecase.getProcessMap();
	}
}
