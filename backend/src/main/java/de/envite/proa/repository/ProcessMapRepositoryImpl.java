package de.envite.proa.repository;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.DataStore;
import de.envite.proa.entities.DataStoreConnection;
import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessMap;
import de.envite.proa.repository.tables.DataStoreConnectionTable;
import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProcessConnectionTable;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.usecases.processmap.ProcessMapRespository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProcessMapRepositoryImpl implements ProcessMapRespository {

	private ProjectDao projectDao;
	private ProcessModelDao processModelDao;
	private ProcessConnectionDao processConnectionDao;
	private DataStoreDao dataStoreDao;
	private DataStoreConnectionDao dataStoreConnectionDao;

	@Inject
	public ProcessMapRepositoryImpl(ProjectDao projectDao, //
			ProcessModelDao processModelDao, //
			ProcessConnectionDao processConnectionDao, //
			DataStoreDao dataStoreDao, //
			DataStoreConnectionDao dataStoreConnectionDao) {
		this.projectDao = projectDao;
		this.processModelDao = processModelDao;
		this.processConnectionDao = processConnectionDao;
		this.dataStoreDao = dataStoreDao;
		this.dataStoreConnectionDao = dataStoreConnectionDao;
	}

	@Override
	public ProcessMap getProcessMap(Long projectId) {
		
		ProjectTable projectTable = projectDao.findById(projectId);
		List<ProcessDetails> processModelInformation = getProcessDetails(projectTable);
		List<ProcessConnection> processConnections = getProcessConnections(projectTable);
		List<DataStore> dataStores = getDataStores(projectTable);
		List<DataStoreConnection> dataStoreConnections = getDataStoreConnections(projectTable);

		ProcessMap map = new ProcessMap();
		map.setConnections(processConnections);
		map.setProcesses(processModelInformation);
		map.setDataStores(dataStores);
		map.setDataStoreConnections(dataStoreConnections);

		return map;
	}

	private List<ProcessDetails> getProcessDetails(ProjectTable projectTable) {

		return processModelDao//
				.getProcessModels(projectTable)//
				.stream()//
				.map(ProcessDetailsMapper::map)//
				.collect(Collectors.toList());
	}

	private List<ProcessConnection> getProcessConnections(ProjectTable projectTable) {
		return processConnectionDao//
				.getProcessConnections(projectTable)//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	private List<DataStore> getDataStores(ProjectTable projectTable) {
		return dataStoreDao//
				.getDataStores(projectTable)//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	private List<DataStoreConnection> getDataStoreConnections(ProjectTable projectTable) {
		return dataStoreConnectionDao//
				.getDataStoreConnections(projectTable)//
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