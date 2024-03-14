package de.envite.proa.repository;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.DataStore;
import de.envite.proa.entities.DataStoreConnection;
import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.entities.ProcessMap;
import de.envite.proa.repository.tables.DataStoreConnectionTable;
import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProcessConnectionTable;
import de.envite.proa.usecases.processmap.ProcessMapRespository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProcessMapRepositoryImpl implements ProcessMapRespository {

	private ProcessModelDao processModelDao;
	private ProcessConnectionDao processConnectionDao;
	private DataStoreDao dataStoreDao;
	private DataStoreConnectionDao dataStoreConnectionDao;

	@Inject
	public ProcessMapRepositoryImpl(ProcessModelDao processModelDao, ProcessConnectionDao processConnectionDao,
			DataStoreDao dataStoreDao, DataStoreConnectionDao dataStoreConnectionDao) {
		this.processModelDao = processModelDao;
		this.processConnectionDao = processConnectionDao;
		this.dataStoreDao = dataStoreDao;
		this.dataStoreConnectionDao = dataStoreConnectionDao;
	}

	@Override
	public ProcessMap getProcessMap() {

		List<ProcessInformation> processModelInformation = getProcessInformation();
		List<ProcessConnection> processConnections = getProcessConnections();
		List<DataStore> dataStores = getDataStores();
		List<DataStoreConnection> dataStoreConnections = getDataStoreConnections();

		ProcessMap map = new ProcessMap();
		map.setConnections(processConnections);
		map.setProcesses(processModelInformation);
		map.setDataStores(dataStores);
		map.setDataStoreConnections(dataStoreConnections);

		return map;
	}

	private List<ProcessInformation> getProcessInformation() {
		return processModelDao//
				.getProcessModels()//
				.stream()//
				.map(model -> new ProcessInformation(//
						model.getId(), //
						model.getName(), //
						model.getDescription(), //
						model.getCreatedAt()))//
				.collect(Collectors.toList());
	}

	private List<ProcessConnection> getProcessConnections() {
		return processConnectionDao//
				.getProcessConnections()//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	private List<DataStore> getDataStores() {
		return dataStoreDao//
				.getDataStores()//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	private List<DataStoreConnection> getDataStoreConnections() {
		return dataStoreConnectionDao//
				.getDataStoreConnections()//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	private DataStore map(DataStoreTable table) {
		DataStore store = new DataStore();
		store.setId(table.getId());
		store.setName(table.getLabel());
		return store;
	}

	private DataStoreConnection map(DataStoreConnectionTable table) {
		DataStoreConnection connection = new DataStoreConnection();
		connection.setProcessid(table.getProcess().getId());
		connection.setDataStoreId(table.getDataStore().getId());
		connection.setAccess(table.getAccess());
		return connection;
	}

	private ProcessConnection map(ProcessConnectionTable table) {
		ProcessConnection connection = new ProcessConnection();
		connection.setCallingProcessid(table.getCallingProcess().getId());
		connection.setCallingElementType(table.getCallingElementType());

		connection.setCalledProcessid(table.getCalledProcess().getId());
		connection.setCalledElementType(table.getCalledElementType());
		return connection;
	}
}
