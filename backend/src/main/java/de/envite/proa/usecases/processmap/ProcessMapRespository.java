package de.envite.proa.usecases.processmap;

import de.envite.proa.entities.DataStoreConnectionWithoutAccess;
import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessMap;
import jakarta.ws.rs.core.Response;

public interface ProcessMapRespository {

	public ProcessMap getProcessMap(Long projectId);

	public Response addConnection(Long projectId, ProcessConnection connection);

	public Response deleteConnection(Long projectId, ProcessConnection connection);

	public Response deleteConnection(Long projectId, DataStoreConnectionWithoutAccess connection);
}
