package de.envite.proa.entities.processmap;

import java.util.ArrayList;
import java.util.List;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.datastore.DataStore;
import de.envite.proa.entities.datastore.DataStoreConnection;
import de.envite.proa.entities.process.ProcessConnection;
import de.envite.proa.entities.process.ProcessDetails;
import lombok.Data;

@Data
public class ProcessMap {

	private List<ProcessDetails> processes = new ArrayList<>();
	private List<ProcessConnection> connections = new ArrayList<>();
	private List<MessageFlowDetails> messageFlows = new ArrayList<>();
	
	private List<DataStore> dataStores = new ArrayList<>();
	private List<DataStoreConnection> dataStoreConnections = new ArrayList<>();
}
