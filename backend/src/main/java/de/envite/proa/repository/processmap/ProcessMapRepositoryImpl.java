package de.envite.proa.repository.processmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.datastore.DataStore;
import de.envite.proa.entities.datastore.DataStoreConnection;
import de.envite.proa.entities.process.*;
import de.envite.proa.entities.processmap.ProcessMap;
import de.envite.proa.repository.datastore.DataStoreConnectionDao;
import de.envite.proa.repository.datastore.DataStoreDao;
import de.envite.proa.repository.messageflow.MessageFlowDao;
import de.envite.proa.repository.messageflow.MessageFlowMapper;
import de.envite.proa.repository.processmodel.*;
import de.envite.proa.repository.project.ProjectDao;
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
	private final MessageFlowDao messageFlowDao;

	@Inject
	public ProcessMapRepositoryImpl(ProjectDao projectDao, //
			ProcessModelDao processModelDao, //
			ProcessConnectionDao processConnectionDao, //
			DataStoreDao dataStoreDao, //
			DataStoreConnectionDao dataStoreConnectionDao, //
			CallActivityDao callActivityDao, //
			ProcessEventDao processEventDao, //
			MessageFlowDao messageFlowDao) {
		this.projectDao = projectDao;
		this.processModelDao = processModelDao;
		this.processConnectionDao = processConnectionDao;
		this.dataStoreDao = dataStoreDao;
		this.dataStoreConnectionDao = dataStoreConnectionDao;
		this.callActivityDao = callActivityDao;
		this.processEventDao = processEventDao;
		this.messageFlowDao = messageFlowDao;
	}

	@Override
	public ProcessMap getProcessMap(Long projectId) {

		ProjectTable projectTable = projectDao.findById(projectId);
		List<ProcessDetails> processModelInformation = getProcessDetailsWithoutCollaborations(projectTable);
		List<ProcessConnection> processConnections = getProcessConnectionsWithoutCollaborations(projectTable);
		List<MessageFlowDetails> messageFlows = getMessageFlows(projectTable);
		List<DataStore> dataStores = getDataStores(projectTable);
		List<DataStoreConnection> dataStoreConnections = getDataStoreConnectionsWithoutCollaborations(projectTable);

		ProcessMap map = new ProcessMap();
		map.setConnections(processConnections);
		map.setMessageFlows(messageFlows);
		map.setProcesses(processModelInformation);
		map.setDataStores(dataStores);
		map.setDataStoreConnections(dataStoreConnections);

		return map;
	}

	@Override
	public void addConnection(Long projectId, ProcessConnection connection) {
		ProcessConnectionTable processConnection = map(projectId, connection);
		processConnectionDao.addConnection(processConnection);
	}

	@Override
	public void copyConnections(Long projectId, Long oldProcessId, Long newProcessId) {
		ProjectTable project = projectDao.findById(projectId);
		ProcessModelTable oldProcess = processModelDao.find(oldProcessId);
		ProcessModelTable newProcess = processModelDao.find(newProcessId);
		List<ProcessConnectionTable> oldConnections = processConnectionDao.getProcessConnections(project, oldProcess);
		List<ProcessConnectionTable> newConnections = processConnectionDao.getProcessConnections(project, newProcess);

		Set<Long> newConnectionSources = new HashSet<>();
		Set<Long> newConnectionTargets = new HashSet<>();
		divideNewConnectionsIntoSets(newConnections, newProcessId, newConnectionSources, newConnectionTargets);

		copyUserCreatedConnections(projectId, oldProcessId, newProcess, oldConnections, newConnectionSources,
				newConnectionTargets);
	}

	private void divideNewConnectionsIntoSets(List<ProcessConnectionTable> newConnections, Long newProcessId,
			Set<Long> newConnectionSources, Set<Long> newConnectionTargets) {
		newConnections.forEach(connection -> {
			boolean isOutgoing = connection.getCallingProcess().getId().equals(newProcessId);
			if (isOutgoing) {
				newConnectionTargets.add(connection.getCalledProcess().getId());
			} else {
				newConnectionSources.add(connection.getCallingProcess().getId());
			}
		});
	}

	private void copyUserCreatedConnections(Long projectId, Long oldProcessId, ProcessModelTable newProcess,
			List<ProcessConnectionTable> oldConnections, Set<Long> newConnectionSources,
			Set<Long> newConnectionTargets) {
		Long newProcessId = newProcess.getId();
		oldConnections.stream().filter(ProcessConnectionTable::getUserCreated).forEach(connection -> {
			boolean isOutgoing = connection.getCallingProcess().getId().equals(oldProcessId);
			if (isOutgoing && newConnectionTargets.contains(connection.getCalledProcess().getId())) {
				return;
			}
			if (isOutgoing && connection.getCalledProcess().getId().equals(newProcessId)) {
				return;
			}
			if (!isOutgoing && newConnectionSources.contains(connection.getCallingProcess().getId())) {
				return;
			}
			if (!isOutgoing && connection.getCallingProcess().getId().equals(newProcessId)) {
				return;
			}
			connection.setCallingProcess(isOutgoing ? newProcess : connection.getCallingProcess());
			connection.setCalledProcess(isOutgoing ? connection.getCalledProcess() : newProcess);
			ProcessConnectionTable newConnection = map(projectId, map(connection));
			processConnectionDao.persist(newConnection);
		});
	}

	@Override
	public void deleteProcessConnection(Long connectionId) {
		processConnectionDao.deleteConnection(connectionId);
	}

	@Override
	public void deleteDataStoreConnection(Long connectionId) {
		dataStoreConnectionDao.deleteConnection(connectionId);
	}

	private List<ProcessDetails> getProcessDetailsWithoutCollaborations(ProjectTable projectTable) {

		return processModelDao//
				.getProcessModels(projectTable)//
				.stream()//
				.filter(pm -> pm.getProcessType() != ProcessType.COLLABORATION)//
				.map(ProcessDetailsMapper::map)//
				.collect(Collectors.toList());
	}

	private List<ProcessConnection> getProcessConnectionsWithoutCollaborations(ProjectTable projectTable) {
		return processConnectionDao//
				.getProcessConnections(projectTable)//
				.stream()//
				.filter(pc -> pc.getCallingProcess().getProcessType() != ProcessType.COLLABORATION//
						&& pc.getCalledProcess().getProcessType() != ProcessType.COLLABORATION)//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	private List<MessageFlowDetails> getMessageFlows(ProjectTable projectTable) {
		return messageFlowDao//
				.getMessageFlows(projectTable)//
				.stream()//
				.map(MessageFlowMapper::map)//
				.collect(Collectors.toList());
	}

	private List<DataStore> getDataStores(ProjectTable projectTable) {
		return dataStoreDao//
				.getDataStores(projectTable)//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	private List<DataStoreConnection> getDataStoreConnectionsWithoutCollaborations(ProjectTable projectTable) {
		return dataStoreConnectionDao//
				.getDataStoreConnections(projectTable)//
				.stream()//
				.filter(dsc -> dsc.getProcess().getProcessType() != ProcessType.COLLABORATION)//
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
		connection.setUserCreated(table.getUserCreated());
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
		table.setUserCreated(connection.getUserCreated());
		return table;
	}

	private String getOrCreateElementId(ProcessElementType elementType, ProcessModelTable processModel) {
		if (elementType == ProcessElementType.CALL_ACTIVITY) {
			CallActivityTable callActivity = callActivityDao.findForProcessModel(processModel);
			if (callActivity == null) {
				return null;
			}
			return callActivity.getElementId();
		}
		EventType eventType = map(elementType);
		ProcessEventTable processEvent = processEventDao.findForProcessModelAndEventType(processModel, eventType);
		if (processEvent == null) {
			return null;
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

	@Override
	public void copyMessageFlowsAndRelations(Long projectId, Long oldProcessId, Long newProcessId) {
		ProjectTable project = projectDao.findById(projectId);
		ProcessModelTable oldProcess = processModelDao.find(oldProcessId);
		ProcessModelTable newProcess = processModelDao.find(newProcessId);
		List<MessageFlowTable> messageFlows = messageFlowDao.getMessageFlows(project, oldProcess);
		for (MessageFlowTable messageFlow : messageFlows) {
			if (messageFlow.getCalledProcess().getId().equals(oldProcessId)) {
				messageFlow.setCalledProcess(newProcess);
			}
			if (messageFlow.getCallingProcess().getId().equals(oldProcessId)) {
				messageFlow.setCallingProcess(newProcess);
			}
			messageFlowDao.merge(messageFlow);
		}

		List<ProcessModelTable> oldParents = new ArrayList<>(oldProcess.getParents());
		for (ProcessModelTable oldParent : oldParents) {
			if (!newProcess.getParents().contains(oldParent)) {
				newProcess.getParents().add(oldParent);
				oldParent.getChildren().add(newProcess);
			}

			oldProcess.getParents().remove(oldParent);
			oldParent.getChildren().remove(oldProcess);

			processModelDao.merge(oldParent);
		}

		List<ProcessModelTable> oldChildren = new ArrayList<>(oldProcess.getChildren());
		for (ProcessModelTable oldChild : oldChildren) {
			if (!newProcess.getChildren().contains(oldChild)) {
				newProcess.getChildren().add(oldChild);
				oldChild.getParents().add(newProcess);
			}

			oldProcess.getChildren().remove(oldChild);
			oldChild.getParents().remove(newProcess);

			processModelDao.merge(oldChild);
		}

		processModelDao.merge(newProcess);
		processModelDao.merge(oldProcess);
	}
}