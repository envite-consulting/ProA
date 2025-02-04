package de.envite.proa.usecases.processmap;

import de.envite.proa.entities.process.ProcessConnection;
import de.envite.proa.entities.processmap.ProcessMap;

public interface ProcessMapRespository {

	public ProcessMap getProcessMap(Long projectId);

	public void addConnection(Long projectId, ProcessConnection connection);

	public void copyConnections(Long projectId, Long oldProcessId, Long newProcessId);

	public void deleteProcessConnection(Long connectionId);

	public void deleteDataStoreConnection(Long connectionId);

	public void copyMessageFlowsAndRelations(Long projectId, Long oldProcessId, Long newProcessId);
}
