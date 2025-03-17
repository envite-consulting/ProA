package de.envite.proa.repository.processmodel;

import de.envite.proa.XmlConverter;
import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.datastore.DataAccess;
import de.envite.proa.entities.process.*;
import de.envite.proa.repository.datastore.DataStoreConnectionDao;
import de.envite.proa.repository.datastore.DataStoreDao;
import de.envite.proa.repository.messageflow.MessageFlowDao;
import de.envite.proa.repository.messageflow.MessageFlowMapper;
import de.envite.proa.repository.tables.*;
import de.envite.proa.usecases.processmodel.ProcessModelRepository;
import de.envite.proa.util.LabelMatcher;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

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
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(projectId);

		ProcessModelTable table = ProcessmodelMapper.map(processModel, projectTable);
		table.setCreatedAt(LocalDateTime.now());
		table.setProject(projectTable);

		String parentBpmnProcessId = processModel.getParentBpmnProcessId();
		processModelDao.persist(table);
		if (parentBpmnProcessId != null) {
			ProcessModelTable parent = processModelDao.findByBpmnProcessIdWithChildren(parentBpmnProcessId,
					projectTable);
			processModelDao.addChild(parent.getId(), table.getId());
		}

		List<ProcessEventTable> processEvents = ProcessmodelMapper.mapEvents(processModel.getEvents(), table,
				projectTable).stream().toList();
		connectEvents(projectTable, processEvents);

		connectCallActivities(processModel, table, projectTable);

		connectDataStores(projectTable, table);

		return table.getId();
	}

	@Override
	public List<ProcessInformation> getProcessInformation(Long projectId) {
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(projectId);

		return processModelDao //
				.getProcessModelsWithChildren(projectTable) //
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
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(projectId);

		messageFlows.forEach(messageFlow -> {
			messageFlowDao.persist(MessageFlowMapper.map(messageFlow, projectTable));
		});
	}

	private void connectDataStores(ProjectTable project, ProcessModelTable processModel) {
		List<ProcessDataStoreTable> dataStores = processModel.getDataStores();
		List<DataStoreTable> projectDataStores = dataStoreDao.getDataStores(project);

		for (ProcessDataStoreTable dataStore : dataStores) {
			DataStoreTable dataStoreToConnect = matchOrCreateDataStoreTable(project, dataStore, projectDataStores);

			createDataStoreConnection(project, processModel, dataStoreToConnect, dataStore.getAccess());
		}
	}

	private DataStoreTable matchOrCreateDataStoreTable(ProjectTable project, ProcessDataStoreTable dataStore,
			List<DataStoreTable> projectDataStores) {
		return projectDataStores
				.stream()
				.<DataStoreTable>mapMulti((projectDataStore, consumer) -> {
					if (LabelMatcher.isSimilar(dataStore.getLabel(), projectDataStore.getLabel())) {
						consumer.accept(projectDataStore);
					}
				})
				.findFirst()
				.orElseGet(() -> createDataStoreTable(project, dataStore.getLabel()));
	}

	private DataStoreTable createDataStoreTable(ProjectTable project, String label) {
		DataStoreTable dataStore = new DataStoreTable();
		dataStore.setProject(project);
		dataStore.setLabel(label);
		dataStoreDao.persist(dataStore);
		return dataStore;
	}

	private void createDataStoreConnection(ProjectTable project, ProcessModelTable processModel,
			DataStoreTable dataStore, DataAccess dataAccess) {
		DataStoreConnectionTable connectionTable = new DataStoreConnectionTable();
		connectionTable.setAccess(dataAccess);
		connectionTable.setProcess(processModel);
		connectionTable.setDataStore(dataStore);
		connectionTable.setProject(project);

		dataStoreConnectionDao.persist(connectionTable);
	}

	private void connectCallActivities(ProcessModel processModel, ProcessModelTable table, ProjectTable projectTable) {
		List<CallActivityTable> processCallActivities = ProcessmodelMapper.map(processModel.getCallActivities(), table,
				projectTable).stream().toList();
		List<ProcessModelTable> projectProcessModels = processModelDao.getProcessModels(projectTable);
		connectCallActivitiesWithProcessModels(projectTable, processCallActivities, projectProcessModels);

		List<CallActivityTable> callActivities = callActivityDao.getCallActivities(projectTable);
		connectCallActivitiesWithProcessModels(projectTable, callActivities, List.of(table));
	}

	private void connectCallActivitiesWithProcessModels(ProjectTable project, List<CallActivityTable> callActivities,
			List<ProcessModelTable> processModels) {
		for (CallActivityTable callActivity : callActivities) {
			for (ProcessModelTable processModel : processModels) {
				if (LabelMatcher.isSimilar(callActivity.getLabel(), processModel.getName())) {
					connectCallActivityWithProcessModel(project, callActivity, processModel);
				}
			}
		}
	}

	private void connectCallActivityWithProcessModel(ProjectTable projectTable, CallActivityTable callActivityTable,
			ProcessModelTable table) {
		ProcessConnectionTable connection = new ProcessConnectionTable();
		connection.setCallingProcess(callActivityTable.getProcessModel());
		connection.setCallingElementType(ProcessElementType.CALL_ACTIVITY);
		connection.setCallingElement(callActivityTable.getElementId());

		connection.setCalledProcess(table);
		connection.setCalledElementType(ProcessElementType.START_EVENT);
		// called element remains empty
		connection.setLabel(callActivityTable.getLabel());
		connection.setProject(projectTable);
		connection.setUserCreated(false);
		processConnectionDao.persist(connection);
	}

	private void connectEvents(ProjectTable project, List<ProcessEventTable> events) {
		if (events.stream().anyMatch(event -> event.getEventType() == EventType.INVALID)) {
			throw new IllegalArgumentException("Unknown event type: " + EventType.INVALID);
		}

		List<ProcessEventTable> throwEvents = new ArrayList<>();
		List<ProcessEventTable> catchEvents = new ArrayList<>();
		divideIntoThrowAndCatchEvents(events, throwEvents, catchEvents);

		List<ProcessEventTable> projectEvents = processEventDao.getEvents(project);
		List<ProcessEventTable> projectThrowEvents = new ArrayList<>();
		List<ProcessEventTable> projectCatchEvents = new ArrayList<>();
		divideIntoThrowAndCatchEvents(projectEvents, projectThrowEvents, projectCatchEvents);

		matchEventsAndCreateConnections(project, throwEvents, projectCatchEvents);
		matchEventsAndCreateConnections(project, projectThrowEvents, catchEvents);
	}

	private void divideIntoThrowAndCatchEvents(List<ProcessEventTable> events, List<ProcessEventTable> throwEvents,
			List<ProcessEventTable> catchEvents) {
		for (ProcessEventTable event : events) {
			if (event.getEventType().equals(EventType.END) || event.getEventType()
					.equals(EventType.INTERMEDIATE_THROW)) {
				throwEvents.add(event);
			} else {
				catchEvents.add(event);
			}
		}
	}

	private void matchEventsAndCreateConnections(ProjectTable project, List<ProcessEventTable> throwEvents,
			List<ProcessEventTable> catchEvents) {
		for (ProcessEventTable throwEvent : throwEvents) {
			for (ProcessEventTable catchEvent : catchEvents) {
				if (LabelMatcher.isSimilar(throwEvent.getLabel(), catchEvent.getLabel())) {
					createConnection(project, throwEvent, catchEvent);
				}
			}
		}
	}

	private void createConnection(ProjectTable project, ProcessEventTable throwEvent, ProcessEventTable catchEvent) {
		ProcessConnectionTable connection = new ProcessConnectionTable();

		connection.setCallingProcess(throwEvent.getProcessModel());
		connection.setCallingElement(throwEvent.getElementId());
		connection.setCallingElementType(ProcessmodelMapper.map(throwEvent.getEventType()));

		connection.setCalledProcess(catchEvent.getProcessModel());
		connection.setCalledElement(catchEvent.getElementId());
		connection.setCalledElementType(ProcessmodelMapper.map(catchEvent.getEventType()));

		connection.setLabel(throwEvent.getLabel());
		connection.setProject(project);
		connection.setUserCreated(false);
		processConnectionDao.persist(connection);
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
		ProjectTable project = new ProjectTable();
		project.setId(projectId);
		return processModelDao.findByNameOrBpmnProcessIdWithoutCollaborations(name, bpmnProcessId, project);
	}
}
