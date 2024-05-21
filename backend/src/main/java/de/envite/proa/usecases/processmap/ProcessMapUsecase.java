package de.envite.proa.usecases.processmap;

import de.envite.proa.entities.DataStoreConnection;
import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessMap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProcessMapUsecase {

	@Inject
	private ProcessMapRespository repository;

	public ProcessMap getProcessMap(Long projectId) {
		return repository.getProcessMap(projectId);
	}

	public void addConnection(Long projectId, ProcessConnection connection) {
		repository.addConnection(projectId, connection);
	}

	public void deleteConnection(Long projectId, ProcessConnection connection) {
		repository.deleteConnection(projectId, connection);
	}

	public void deleteConnection(Long projectId, DataStoreConnection connection) {
		repository.deleteConnection(projectId, connection);
	}
}