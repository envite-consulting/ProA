package de.envite.proa.repository.processmodel;

import de.envite.proa.XmlConverter;
import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.process.*;
import de.envite.proa.repository.datastore.DataStoreConnectionDao;
import de.envite.proa.repository.datastore.DataStoreDao;
import de.envite.proa.repository.messageflow.MessageFlowDao;
import de.envite.proa.repository.messageflow.MessageFlowMapper;
import de.envite.proa.repository.tables.*;
import de.envite.proa.usecases.processmodel.ProcessModelRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequestScoped
public class ProcessmodelRepositoryImpl implements ProcessModelRepository {

	private ProcessModelDao processModelDao;
	private DataStoreDao dataStoreDao;
	private DataStoreConnectionDao dataStoreConnectionDao;
	private CallActivityDao callActivityDao;
	private ProcessConnectionDao processConnectionDao;
	private ProcessEventDao processEventDao;
	private MessageFlowDao messageFlowDao;

	@Inject
	public ProcessmodelRepositoryImpl(//
			ProcessModelDao processModelDao, //
			DataStoreDao dataStoreDao, //
			DataStoreConnectionDao dataStoreConnectionDao, //
			CallActivityDao callActivityDao, //
			ProcessConnectionDao processConnectionDao, //
			ProcessEventDao processEventDao, //
			MessageFlowDao messageFlowDao
	) {
		this.processModelDao = processModelDao;
		this.dataStoreDao = dataStoreDao;
		this.dataStoreConnectionDao = dataStoreConnectionDao;
		this.callActivityDao = callActivityDao;
		this.processConnectionDao = processConnectionDao;
		this.processEventDao = processEventDao;
		this.messageFlowDao = messageFlowDao;
	}

	@Override
	public Long saveProcessModel(Long projectId, ProcessModel processModel) {
		ProjectVersionTable projectVersionTable = new ProjectVersionTable();
		projectVersionTable.setId(projectId);

		ProcessModelTable table = ProcessmodelMapper.map(processModel, projectVersionTable);
		table.setCreatedAt(LocalDateTime.now());
		table.setProject(projectVersionTable);

		String parentBpmnProcessId = processModel.getParentBpmnProcessId();
		processModelDao.persist(table);
		if (parentBpmnProcessId != null) {
			ProcessModelTable parent = processModelDao.findByBpmnProcessIdWithChildren(parentBpmnProcessId,
					projectVersionTable);
			processModelDao.addChild(parent.getId(), table.getId());
		}

		connectEvents(processModel, table, projectVersionTable);

		connectCallActivities(processModel, table, projectVersionTable);

		connectDataStores(table, projectVersionTable);

		return table.getId();
	}

	@Override
	public List<ProcessInformation> getProcessInformation(Long projectId) {
		ProjectVersionTable projectVersionTable = new ProjectVersionTable();
		projectVersionTable.setId(projectId);

		return processModelDao //
				.getProcessModelsWithChildren(projectVersionTable) //
				.stream() //
				.map(ProcessmodelMapper::map) //
				.toList();
	}

	@Override
	public ProcessDetails getProcessDetails(Long id) {
		ProcessModelTable table = processModelDao.findWithEventsAndActivities(id);
		return ProcessDetailsMapper.map(table);
	}

	@Override
	public void saveMessageFlows(List<MessageFlowDetails> messageFlows, Long projectId) {
		ProjectVersionTable projectVersionTable = new ProjectVersionTable();
		projectVersionTable.setId(projectId);

		messageFlows.forEach(messageFlow -> {
			messageFlowDao.persist(MessageFlowMapper.map(messageFlow, projectVersionTable));
		});
	}

	private void connectDataStores(ProcessModelTable table, ProjectVersionTable projectVersionTable) {
		table//
				.getDataStores()//
				.stream()//
				.forEach(store -> {
					connectProcessDataStore(store, table, projectVersionTable);
				});
	}

	private void connectProcessDataStore(ProcessDataStoreTable store, ProcessModelTable table,
			ProjectVersionTable projectVersionTable) {
		DataStoreTable dataStoreTable;
		try {
			dataStoreTable = dataStoreDao.getDataStoreForLabel(store.getLabel(), projectVersionTable);
		} catch (NoResultException e) {
			dataStoreTable = new DataStoreTable();
			dataStoreTable.setLabel(store.getLabel());
			dataStoreTable.setProject(projectVersionTable);
			dataStoreDao.persist(dataStoreTable);
		}

		DataStoreConnectionTable connectionTable = new DataStoreConnectionTable();
		connectionTable.setAccess(store.getAccess());
		connectionTable.setProcess(table);
		connectionTable.setDataStore(dataStoreTable);
		connectionTable.setProject(projectVersionTable);

		dataStoreConnectionDao.persist(connectionTable);
	}

	private void connectCallActivities(ProcessModel processModel, ProcessModelTable table,
			ProjectVersionTable projectVersionTable) {
		processModel//
				.getCallActivities()//
				.forEach(activity -> {
					connectCallActivityWithProcess(table, activity, projectVersionTable);
				});

		connectProcessWithCallActivity(table, projectVersionTable);
	}

	private void connectProcessWithCallActivity(ProcessModelTable table, ProjectVersionTable projectVersionTable) {
		List<CallActivityTable> callActivityTables = callActivityDao.getCallActivitiesForName(table.getName(),
				projectVersionTable);

		callActivityTables.forEach(callActivityTable -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(callActivityTable.getProcessModel());
			connection.setCallingElementType(ProcessElementType.CALL_ACTIVITY);
			connection.setCallingElement(callActivityTable.getElementId());

			connection.setCalledProcess(table);
			connection.setCalledElementType(ProcessElementType.START_EVENT);
			// called element remains empty
			connection.setLabel(callActivityTable.getLabel());
			connection.setProject(projectVersionTable);
			connection.setUserCreated(false);
			processConnectionDao.persist(connection);
		});

	}

	private void connectCallActivityWithProcess(ProcessModelTable table, ProcessActivity activity,
			ProjectVersionTable projectVersionTable) {
		List<ProcessModelTable> processModelTable = processModelDao.getProcessModelsForName(activity.getLabel(),
				projectVersionTable);

		processModelTable.forEach(process -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(table);
			connection.setCallingElement(activity.getElementId());
			connection.setCallingElementType(ProcessElementType.CALL_ACTIVITY);
			connection.setCalledProcess(process);
			// When the entire process is called, the called process is started using the
			// start event
			connection.setCalledElementType(ProcessElementType.START_EVENT);
			// called element remains empty
			connection.setLabel(process.getName());
			connection.setProject(projectVersionTable);
			connection.setUserCreated(false);
			processConnectionDao.persist(connection);
		});
	}

	private void connectEvents(ProcessModel processModel, ProcessModelTable table,
			ProjectVersionTable projectVersionTable) {
		processModel//
				.getEvents()//
				.forEach(event -> {
					connectEvents(table, event, projectVersionTable);
				});
	}

	private void connectEvents(ProcessModelTable table, ProcessEvent event, ProjectVersionTable projectVersionTable) {
		switch (event.getEventType()) {
			case START:
			case INTERMEDIATE_CATCH:
				connectWithThrowEvents(table, event, EventType.INTERMEDIATE_THROW, projectVersionTable);
				connectWithThrowEvents(table, event, EventType.END, projectVersionTable);
				break;
			case INTERMEDIATE_THROW:
			case END:
				connectWithCatchEvents(table, event, EventType.START, projectVersionTable);
				connectWithCatchEvents(table, event, EventType.INTERMEDIATE_CATCH, projectVersionTable);
				break;
			default:
				throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
		}
	}

	private void connectWithCatchEvents(ProcessModelTable newTable, ProcessEvent newThrowEvent,
			EventType eventTypeToConnectTo, ProjectVersionTable projectVersionTable) {
		List<ProcessEventTable> startEventsWithSameLabel = processEventDao
				.getEventsForLabelAndType(newThrowEvent.getLabel(), eventTypeToConnectTo, projectVersionTable);

		startEventsWithSameLabel.forEach(event -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(newTable);
			connection.setCallingElement(newThrowEvent.getElementId());
			if (newThrowEvent.getEventType().equals(EventType.INTERMEDIATE_THROW)) {
				connection.setCallingElementType(ProcessElementType.INTERMEDIATE_THROW_EVENT);
			} else {
				connection.setCallingElementType(ProcessElementType.END_EVENT);
			}

			connection.setCalledProcess(event.getProcessModel());
			connection.setCalledElement(event.getElementId());
			if (eventTypeToConnectTo.equals(EventType.START)) {
				connection.setCalledElementType(ProcessElementType.START_EVENT);
			} else {
				connection.setCalledElementType(ProcessElementType.INTERMEDIATE_CATCH_EVENT);
			}

			connection.setLabel(event.getLabel());
			connection.setProject(projectVersionTable);
			connection.setUserCreated(false);
			processConnectionDao.persist(connection);
		});
	}

	private void connectWithThrowEvents(ProcessModelTable newTable, ProcessEvent newEvent,
			EventType eventTypeForConnectionFrom, ProjectVersionTable projectVersionTable) {
		List<ProcessEventTable> endEventsWithSameLabel = processEventDao.getEventsForLabelAndType(newEvent.getLabel(),
				eventTypeForConnectionFrom, projectVersionTable);

		endEventsWithSameLabel.forEach(event -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(event.getProcessModel());
			connection.setCallingElement(event.getElementId());
			if (eventTypeForConnectionFrom.equals(EventType.END)) {
				connection.setCallingElementType(ProcessElementType.END_EVENT);
			} else {
				connection.setCallingElementType(ProcessElementType.INTERMEDIATE_THROW_EVENT);
			}

			connection.setCalledProcess(newTable);
			connection.setCalledElement(newEvent.getElementId());
			if (newEvent.getEventType().equals(EventType.START)) {
				connection.setCalledElementType(ProcessElementType.START_EVENT);
			} else {
				connection.setCalledElementType(ProcessElementType.INTERMEDIATE_CATCH_EVENT);
			}

			connection.setLabel(event.getLabel());
			connection.setProject(projectVersionTable);
			connection.setUserCreated(false);
			processConnectionDao.persist(connection);
		});
	}

	@Override
	public ProcessModelTable getProcessModel(Long id) {
		return processModelDao.find(id);
	}

	@Override
	public String getProcessModelXml(Long id) {
		byte[] xmlBytes = processModelDao.getBpmnXml(id);
		return XmlConverter.bytesToString(xmlBytes);
	}

	@Override
	public void deleteProcessModel(Long id) {
		List<Long> relatedProcessModelIdsToDelete = getRelatedProcessModelsToDelete(id, new ArrayList<>());
		relatedProcessModelIdsToDelete.forEach(processModelId -> {
			dataStoreConnectionDao.deleteForProcessModel(processModelId);
			processConnectionDao.deleteForProcessModel(processModelId);
			messageFlowDao.deleteForProcessModel(processModelId);
		});

		processModelDao.delete(relatedProcessModelIdsToDelete);
	}

	private List<Long> getRelatedProcessModelsToDelete(Long id, List<Long> processModelIdsToDelete) {
		if (processModelIdsToDelete.contains(id)) {
			return processModelIdsToDelete;
		}

		processModelIdsToDelete.add(id);
		ProcessModelTable processModelWithParentsAndChildren = processModelDao.findWithParentsAndChildren(id);
		Set<ProcessModelTable> children = processModelWithParentsAndChildren.getChildren();
		Set<ProcessModelTable> parents = processModelWithParentsAndChildren.getParents();

		for (ProcessModelTable child : children) {
			ProcessModelTable childWithParents = processModelDao.findWithParents(child.getId());
			boolean hasOtherParent = childWithParents.getParents().size() > 1;
			if (!hasOtherParent) {
				processModelIdsToDelete = getRelatedProcessModelsToDelete(child.getId(), processModelIdsToDelete);
			}
		}

		for (ProcessModelTable parent : parents) {
			processModelIdsToDelete = getRelatedProcessModelsToDelete(parent.getId(), processModelIdsToDelete);
		}

		return processModelIdsToDelete;
	}

	@Override
	public ProcessModelTable findByNameOrBpmnProcessIdWithoutCollaborations(String name, String bpmnProcessId,
			Long projectId) {
		ProjectVersionTable project = new ProjectVersionTable();
		project.setId(projectId);
		return processModelDao.findByNameOrBpmnProcessIdWithoutCollaborations(name, bpmnProcessId, project);
	}
}
