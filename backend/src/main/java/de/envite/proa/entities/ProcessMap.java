package de.envite.proa.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ProcessMap {

	private List<ProcessDetails> processes = new ArrayList<>();
	private List<ProcessConnection> connections = new ArrayList<>();
	private List<MessageFlowDetails> messageFlows = new ArrayList<>();
	
	private List<DataStore> dataStores = new ArrayList<>();
	private List<DataStoreConnection> dataStoreConnections = new ArrayList<>();
}
