package de.envite.proa.usecases.processmap;

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

	public void deleteProcessConnection(Long connectionId) {
		repository.deleteProcessConnection(connectionId);
	}

	public void deleteDataStoreConnection(Long connectionId) {
		repository.deleteDataStoreConnection(connectionId);
	}
}