package de.envite.proa.rest;

import de.envite.proa.security.RolesAllowedIfWebVersion;
import org.jboss.resteasy.reactive.RestPath;

import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessMap;
import de.envite.proa.usecases.processmap.ProcessMapUsecase;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public class ProcessMapResource {

	@Inject
	private ProcessMapUsecase usecase;

	@GET
	@Path("/project/{projectId}/process-map")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public ProcessMap getProcessMap(@RestPath Long projectId) {
		return usecase.getProcessMap(projectId);
	}

	@POST
	@Path("/project/{projectId}/process-map/connection")
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public void addConnection(@RestPath Long projectId, ProcessConnection connection) {
		usecase.addConnection(projectId, connection);
	}

	@DELETE
	@Path("/project/process-map/process-connection/{connectionId}")
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public void deleteProcessConnection(@RestPath Long connectionId) {
		usecase.deleteProcessConnection(connectionId);
	}

	@DELETE
	@Path("/project/process-map/datastore-connection/{connectionId}")
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public void deleteDataStoreConnection(@RestPath Long connectionId) {
		usecase.deleteDataStoreConnection(connectionId);
	}
}
