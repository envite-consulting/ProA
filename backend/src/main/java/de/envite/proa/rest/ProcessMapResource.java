package de.envite.proa.rest;

import org.jboss.resteasy.reactive.RestPath;

import de.envite.proa.entities.DataStoreConnection;
import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessMap;
import de.envite.proa.usecases.processmap.ProcessMapUsecase;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
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

	@POST
	@Path("/project/{projectId}/process-map/connection")
	public void addConnection(@RestPath Long projectId, ProcessConnection connection) {
		usecase.addConnection(projectId, connection);
	}

	@PATCH
	@Path("/project/{projectId}/process-map/connection")
	public void deleteConnection(@RestPath Long projectId, ProcessConnection connection) {
		usecase.deleteConnection(projectId, connection);
	}

	@PATCH
	@Path("/project/{projectId}/process-map/datastore-connection")
	public void deleteConnection(@RestPath Long projectId, DataStoreConnection connection) {
		usecase.deleteConnection(projectId, connection);
	}
}
