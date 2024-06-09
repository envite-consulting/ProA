package de.envite.proa.repository;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.*;
import de.envite.proa.repository.tables.*;
import de.envite.proa.usecases.processmap.ProcessMapRespository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProcessMapRepositoryImpl implements ProcessMapRespository {

	private final ProjectDao projectDao;
	private final ProcessModelDao processModelDao;
	private final ProcessConnectionDao processConnectionDao;
	private final DataStoreDao dataStoreDao;
	private final DataStoreConnectionDao dataStoreConnectionDao;
	private final CallActivityDao callActivityDao;
	private final ProcessEventDao processEventDao;

	@Inject
	public ProcessMapRepositoryImpl(ProjectDao projectDao, //
                                    ProcessModelDao processModelDao, //
                                    ProcessConnectionDao processConnectionDao, //
                                    DataStoreDao dataStoreDao, //
                                    DataStoreConnectionDao dataStoreConnectionDao, //
									CallActivityDao callActivityDao, //
									ProcessEventDao processEventDao //
	) {
		this.projectDao = projectDao;
		this.processModelDao = processModelDao;
		this.processConnectionDao = processConnectionDao;
		this.dataStoreDao = dataStoreDao;
		this.dataStoreConnectionDao = dataStoreConnectionDao;
        this.callActivityDao = callActivityDao;
        this.processEventDao = processEventDao;
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

	@Override
	public void addConnection(Long projectId, ProcessConnection connection) {
		System.out.println("adding connection");
		ProcessConnectionTable processConnection = map(projectId, connection);
		processConnectionDao.addConnection(processConnection);
	}

	@Override
	public void deleteProcessConnection(Long connectionId) {
		processConnectionDao.deleteConnection(connectionId);
	}

	@Override
	public void deleteDataStoreConnection(Long connectionId) {
		dataStoreConnectionDao.deleteConnection(connectionId);
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
		connection.setId(table.getId());
		connection.setProcessid(table.getProcess().getId());
		connection.setDataStoreId(table.getDataStore().getId());
		connection.setAccess(table.getAccess());
		return connection;
	}

	private ProcessConnection map(ProcessConnectionTable table) {
		ProcessConnection connection = new ProcessConnection();
		connection.setId(table.getId());
		connection.setCallingProcessid(table.getCallingProcess().getId());
		connection.setCallingElementType(table.getCallingElementType());

		connection.setCalledProcessid(table.getCalledProcess().getId());
		connection.setCalledElementType(table.getCalledElementType());
		
		connection.setLabel(table.getLabel());
		return connection;
	}

	private ProcessConnectionTable map(Long projectId, ProcessConnection connection) {
		ProcessElementType callingElementType = connection.getCallingElementType();
		ProcessElementType calledElementType = connection.getCalledElementType();
		ProcessModelTable callingProcess = processModelDao.find(connection.getCallingProcessid());
		ProcessModelTable calledProcess = processModelDao.find(connection.getCalledProcessid());
		String callingElement = getOrCreateElementId(callingElementType, callingProcess);
		String calledElement = getOrCreateElementId(calledElementType, calledProcess);

		ProcessConnectionTable table = new ProcessConnectionTable();
		table.setCallingProcess(callingProcess);
		table.setCalledProcess(calledProcess);
		table.setCallingElementType(callingElementType);
		table.setCalledElementType(calledElementType);
		table.setCallingElement(callingElement);
		table.setCalledElement(calledElement);
		table.setProject(projectDao.findById(projectId));
		return table;
	}

	private String getOrCreateElementId(ProcessElementType elementType, ProcessModelTable processModel) {
		if (elementType == ProcessElementType.CALL_ACTIVITY) {
			CallActivityTable callActivity = callActivityDao.findForProcessModel(processModel);
			if (callActivity == null) {
				return "";
			}
			return callActivity.getElementId();
		}
		EventType eventType = map(elementType);
		ProcessEventTable processEvent = processEventDao.findForProcessModelAndEventType(processModel, eventType);
		if (processEvent == null) {
			return "";
		}
		return processEvent.getElementId();
	}

	private EventType map(ProcessElementType elementType) {
        return switch (elementType) {
			case ProcessElementType.START_EVENT -> EventType.START;
			case ProcessElementType.INTERMEDIATE_CATCH_EVENT -> EventType.INTERMEDIATE_CATCH;
			case ProcessElementType.INTERMEDIATE_THROW_EVENT -> EventType.INTERMEDIATE_THROW;
			case ProcessElementType.END_EVENT -> EventType.END;
            default -> null;
        };
    }
}