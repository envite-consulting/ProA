package de.envite.proa.usecases.processmap;

import de.envite.proa.entities.DataStoreConnection;
import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessMap;

public interface ProcessMapRespository {

	public ProcessMap getProcessMap(Long projectId);

	public void addConnection(Long projectId, ProcessConnection connection);

	public void deleteConnection(Long projectId, ProcessConnection connection);

	public void deleteConnection(Long projectId, DataStoreConnection connection);
}
