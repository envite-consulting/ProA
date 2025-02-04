package de.envite.proa.usecases.processmap;

import de.envite.proa.entities.process.ProcessConnection;
import de.envite.proa.entities.processmap.ProcessMap;
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

	public void copyConnections(Long projectId ,Long oldProcessId, Long newProcessId) {
		repository.copyConnections(projectId, oldProcessId, newProcessId);
	}

	public void deleteProcessConnection(Long connectionId) {
		repository.deleteProcessConnection(connectionId);
	}

	public void deleteDataStoreConnection(Long connectionId) {
		repository.deleteDataStoreConnection(connectionId);
	}
}