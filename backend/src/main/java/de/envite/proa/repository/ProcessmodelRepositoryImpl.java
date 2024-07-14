package de.envite.proa.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.EventType;
import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessElementType;
import de.envite.proa.entities.ProcessEvent;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.entities.ProcessModel;
import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.DataStoreConnectionTable;
import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProcessConnectionTable;
import de.envite.proa.repository.tables.ProcessDataStoreTable;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.usecases.ProcessModelRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

@ApplicationScoped
public class ProcessmodelRepositoryImpl implements ProcessModelRepository {

	private ProjectDao projectDao;
	private ProcessModelDao processModelDao;
	private DataStoreDao dataStoreDao;
	private DataStoreConnectionDao dataStoreConnectionDao;
	private CallActivityDao callActivityDao;
	private ProcessConnectionDao processConnectionDao;
	private ProcessEventDao processEventDao;

	@Inject
	public ProcessmodelRepositoryImpl(ProjectDao projectDao, //
			ProcessModelDao processModelDao, //
			DataStoreDao dataStoreDao, //
			DataStoreConnectionDao dataStoreConnectionDao, //
			CallActivityDao callActivityDao, //
			ProcessConnectionDao processConnectionDao, //
			ProcessEventDao processEventDao) {
		this.projectDao = projectDao;
		this.processModelDao = processModelDao;
		this.dataStoreDao = dataStoreDao;
		this.dataStoreConnectionDao = dataStoreConnectionDao;
		this.callActivityDao = callActivityDao;
		this.processConnectionDao = processConnectionDao;
		this.processEventDao = processEventDao;
	}

	@Override
	public Long saveProcessModel(Long projectId, ProcessModel processModel) {
		
		ProjectTable projectTable = projectDao.findById(projectId);

		ProcessModelTable table = ProcessmodelMapper.map(processModel, projectTable);
		table.setCreatedAt(LocalDateTime.now());
		table.setProject(projectTable);
		processModelDao.persist(table);

		connectEvents(processModel, table, projectTable);

		connectCallActivities(processModel, table, projectTable);

		connectDataStores(table, projectTable);

		return table.getId();
	}

	@Override
	public List<ProcessInformation> getProcessInformation(Long projectId) {
		
		ProjectTable projectTable = projectDao.findById(projectId);
		return processModelDao//
				.getProcessModels(projectTable)//
				.stream()//
				.map(model -> new ProcessInformation(//
						model.getId(), //
						model.getName(), //
						model.getDescription(), //
						model.getCreatedAt()))//
				.collect(Collectors.toList());
	}

	@Override
	public ProcessDetails getProcessDetails(Long id) {

		ProcessModelTable table = processModelDao.findWithChildren(id);
		ProcessDetails details = ProcessDetailsMapper.map(table);

		return details;
	}

	private void connectDataStores(ProcessModelTable table, ProjectTable projectTable) {

		table//
				.getDataStores()//
				.stream()//
				.forEach(store -> {
					connectProcessDataStore(store, table, projectTable);
				});

	}

	private void connectProcessDataStore(ProcessDataStoreTable store, ProcessModelTable table, ProjectTable projectTable) {
		DataStoreTable dataStoreTable;
		try {

			dataStoreTable = dataStoreDao.getDataStoreForLabel(store.getLabel(), projectTable);
		} catch (NoResultException e) {
			dataStoreTable = new DataStoreTable();
			dataStoreTable.setLabel(store.getLabel());
			dataStoreTable.setProject(projectTable);
			dataStoreDao.persist(dataStoreTable);
		}

		DataStoreConnectionTable connectionTable = new DataStoreConnectionTable();
		connectionTable.setAccess(store.getAccess());
		connectionTable.setProcess(table);
		connectionTable.setDataStore(dataStoreTable);
		connectionTable.setProject(projectTable);

		dataStoreConnectionDao.persist(connectionTable);
	}

	private void connectCallActivities(ProcessModel processModel, ProcessModelTable table, ProjectTable projectTable) {
		processModel//
				.getCallActivities()//
				.forEach(activity -> {
					connectCallActivityWithProcess(table, activity, projectTable);
				});

		connectProcessWithCallActivity(table, projectTable);
	}

	private void connectProcessWithCallActivity(ProcessModelTable table, ProjectTable projectTable) {
		List<CallActivityTable> callActivityTables = callActivityDao.getCallActivitiesForName(table.getName(), projectTable);

		callActivityTables.forEach(callActivityTable -> {
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
		});
	}

	private void connectCallActivityWithProcess(ProcessModelTable table, ProcessActivity activity, ProjectTable projectTable) {
		List<ProcessModelTable> processModelTable = processModelDao.getProcessModelsForName(activity.getLabel(), projectTable);

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
			connection.setProject(projectTable);
			connection.setUserCreated(false);
			processConnectionDao.persist(connection);
		});
	}

	private void connectEvents(ProcessModel processModel, ProcessModelTable table, ProjectTable projecTable) {
		processModel//
				.getEvents()//
				.forEach(event -> {
					connectEvents(table, event, projecTable);
				});
	}

	private void connectEvents(ProcessModelTable table, ProcessEvent event, ProjectTable projectTable) {
		switch (event.getEventType()) {
		case START:
		case INTERMEDIATE_CATCH:
			connectWithThrowEvents(table, event, EventType.INTERMEDIATE_THROW, projectTable);
			connectWithThrowEvents(table, event, EventType.END, projectTable);
			break;
		case INTERMEDIATE_THROW:
		case END:
			connectWithCatchEvents(table, event, EventType.START, projectTable);
			connectWithCatchEvents(table, event, EventType.INTERMEDIATE_CATCH, projectTable);
			break;
		}
	}

	private void connectWithCatchEvents(ProcessModelTable newTable, ProcessEvent newThrowEvent,
			EventType eventTypeToConnectTo, ProjectTable projectTable) {
		List<ProcessEventTable> startEventsWithSameLabel = processEventDao
				.getEventsForLabelAndType(newThrowEvent.getLabel(), eventTypeToConnectTo, projectTable);

		startEventsWithSameLabel.forEach(event -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(newTable);
			connection.setCallingElement(newThrowEvent.getElementId());
			if (newThrowEvent.getEventType().equals(EventType.INTERMEDIATE_THROW)) {
				connection.setCallingElementType(ProcessElementType.INTERMEDIATE_THROW_EVENT);
			} else if (newThrowEvent.getEventType().equals(EventType.END)) {
				connection.setCallingElementType(ProcessElementType.END_EVENT);
			}

			connection.setCalledProcess(event.getProcessModel());
			connection.setCalledElement(event.getElementId());
			if (eventTypeToConnectTo.equals(EventType.START)) {
				connection.setCalledElementType(ProcessElementType.START_EVENT);
			} else if (eventTypeToConnectTo.equals(EventType.INTERMEDIATE_CATCH)) {
				connection.setCalledElementType(ProcessElementType.INTERMEDIATE_CATCH_EVENT);
			}

			connection.setLabel(event.getLabel());
			connection.setProject(projectTable);
			connection.setUserCreated(false);
			processConnectionDao.persist(connection);
		});
	}

	private void connectWithThrowEvents(ProcessModelTable newTable, ProcessEvent newEvent,
			EventType eventTypeForConnectionFrom, ProjectTable projectTable) {

		List<ProcessEventTable> endEventsWithSameLabel = processEventDao.getEventsForLabelAndType(newEvent.getLabel(),
				eventTypeForConnectionFrom, projectTable);

		endEventsWithSameLabel.forEach(event -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(event.getProcessModel());
			connection.setCallingElement(event.getElementId());
			if (eventTypeForConnectionFrom.equals(EventType.END)) {
				connection.setCallingElementType(ProcessElementType.END_EVENT);
			} else if (eventTypeForConnectionFrom.equals(EventType.INTERMEDIATE_THROW)) {
				connection.setCallingElementType(ProcessElementType.INTERMEDIATE_THROW_EVENT);
			}

			connection.setCalledProcess(newTable);
			connection.setCalledElement(newEvent.getElementId());
			if (newEvent.getEventType().equals(EventType.START)) {
				connection.setCalledElementType(ProcessElementType.START_EVENT);
			} else if (newEvent.getEventType().equals(EventType.INTERMEDIATE_CATCH)) {
				connection.setCalledElementType(ProcessElementType.INTERMEDIATE_CATCH_EVENT);
			}

			connection.setLabel(event.getLabel());
			connection.setProject(projectTable);
			connection.setUserCreated(false);
			processConnectionDao.persist(connection);
		});
	}

	@Override
	public String getProcessModel(Long id) {
		return processModelDao.find(id).getBpmnXml();
	}

	@Override
	public void deleteProcessModel(Long id) {
		dataStoreConnectionDao.deleteForProcessModel(id);
		processConnectionDao.deleteForProcessModel(id);
		processModelDao.delete(id);
	}
}