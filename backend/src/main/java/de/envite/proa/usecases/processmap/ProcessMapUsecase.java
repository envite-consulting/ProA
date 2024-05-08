package de.envite.proa.usecases.processmap;

import de.envite.proa.entities.DataStoreConnectionWithoutAccess;
import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessMap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ProcessMapUsecase {

	@Inject
	private ProcessMapRespository repository;

	public ProcessMap getProcessMap(Long projectId) {
		return repository.getProcessMap(projectId);
	}

	public Response addConnection(Long projectId, ProcessConnection connection) {
		return repository.addConnection(projectId, connection);
	}

	public Response deleteConnection(Long projectId, ProcessConnection connection) {
		return repository.deleteConnection(projectId, connection);
	}

	public Response deleteConnection(Long projectId, DataStoreConnectionWithoutAccess connection) {
		return repository.deleteConnection(projectId, connection);
	}
}