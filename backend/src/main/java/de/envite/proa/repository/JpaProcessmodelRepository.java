package de.envite.proa.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.DataStore;
import de.envite.proa.entities.DataStoreConnection;
import de.envite.proa.entities.EventType;
import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessDataStore;
import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessElementType;
import de.envite.proa.entities.ProcessEvent;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.entities.ProcessMap;
import de.envite.proa.entities.ProcessModel;
import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.DataStoreConnectionTable;
import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProcessConnectionTable;
import de.envite.proa.repository.tables.ProcessDataStoreTable;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.usecases.ProcessModelRepository;
import de.envite.proa.usecases.processmap.ProcessMapRespository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class JpaProcessmodelRepository implements ProcessModelRepository, ProcessMapRespository {

	private EntityManager em;

	@Inject
	public JpaProcessmodelRepository(EntityManager em) {
		this.em = em;
	}

	@Override
	@Transactional
	public Long saveProcessModel(ProcessModel processModel) {

		ProcessModelTable table = ProcessmodelMapper.map(processModel);
		table.setCreatedAt(LocalDateTime.now());
		em.persist(table);

		connectEvents(processModel, table);

		connectCallActivities(processModel, table);

		connectDataStores(table);

		em.flush();
		return table.getId();
	}

	private void connectDataStores(ProcessModelTable table) {

		table//
				.getDataStores()//
				.stream()//
				.forEach(store -> {
					connectProcessDataStore(store, table);
				});

	}

	private void connectProcessDataStore(ProcessDataStoreTable store, ProcessModelTable table) {
		DataStoreTable dataStoreTable;
		try {

			dataStoreTable = em
					.createQuery("SELECT d FROM DataStoreTable d WHERE d.label = :label", DataStoreTable.class)
					.setParameter("label", store.getLabel())//
					.getSingleResult();
		} catch (NoResultException e) {
			dataStoreTable = new DataStoreTable();
			dataStoreTable.setLabel(store.getLabel());
			em.persist(dataStoreTable);
		}

		DataStoreConnectionTable connectionTable = new DataStoreConnectionTable();
		connectionTable.setAccess(store.getAccess());
		connectionTable.setProcess(table);
		connectionTable.setDataStore(dataStoreTable);

		em.persist(connectionTable);
	}

	private void connectCallActivities(ProcessModel processModel, ProcessModelTable table) {
		processModel//
				.getCallActivities()//
				.forEach(activity -> {
					connectCallActivityWithProcess(table, activity);
				});

		connectProcessWithCallActivity(table);
	}

	private void connectProcessWithCallActivity(ProcessModelTable table) {
		List<CallActivityTable> callActivityTables = em
				.createQuery("SELECT c FROM CallActivityTable c WHERE c.label = :label", CallActivityTable.class)
				.setParameter("label", table.getName())//
				.getResultList();

		callActivityTables.forEach(callActivityTable -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(callActivityTable.getProcessModel());
			connection.setCallingElementType(ProcessElementType.CALL_ACTIVITY);
			connection.setCallingElement(callActivityTable.getElementId());

			connection.setCalledProcess(table);
			connection.setCalledElementType(ProcessElementType.START_EVENT);
			// called element remains empty
			em.persist(connection);
		});
	}

	private void connectCallActivityWithProcess(ProcessModelTable table, ProcessActivity activity) {
		List<ProcessModelTable> processModelTable = em
				.createQuery("SELECT p FROM ProcessModelTable p WHERE p.name = :name", ProcessModelTable.class)
				.setParameter("name", activity.getLabel())//
				.getResultList();

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
			em.persist(connection);
		});

	}

	private void connectEvents(ProcessModel processModel, ProcessModelTable table) {
		processModel//
				.getEvents()//
				.forEach(event -> {
					connectEvents(table, event);
				});
	}

	private void connectEvents(ProcessModelTable table, ProcessEvent event) {
		switch (event.getEventType()) {
		case START:
		case INTERMEDIATE_CATCH:
			connectWithThrowEvents(table, event, EventType.INTERMEDIATE_THROW);
			connectWithThrowEvents(table, event, EventType.END);
			break;
		case INTERMEDIATE_THROW:
		case END:
			connectWithCatchEvents(table, event, EventType.START);
			connectWithCatchEvents(table, event, EventType.INTERMEDIATE_CATCH);
			break;
		}
	}

	private void connectWithCatchEvents(ProcessModelTable newTable, ProcessEvent newThrowEvent,
			EventType eventTypeToConnectTo) {
		List<ProcessEventTable> startEventsWithSameLabel = em
				.createQuery("SELECT e FROM ProcessEventTable e WHERE e.label = :label AND e.eventType=:eventType",
						ProcessEventTable.class)
				.setParameter("label", newThrowEvent.getLabel())//
				.setParameter("eventType", eventTypeToConnectTo)//
				.getResultList();

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

			em.persist(connection);
		});
	}

	private void connectWithThrowEvents(ProcessModelTable newTable, ProcessEvent newEvent,
			EventType eventTypeForConnectionFrom) {
		List<ProcessEventTable> endEventsWithSameLabel = em
				.createQuery("SELECT e FROM ProcessEventTable e WHERE e.label = :label AND e.eventType=:eventType",
						ProcessEventTable.class)
				.setParameter("label", newEvent.getLabel())//
				.setParameter("eventType", eventTypeForConnectionFrom)//
				.getResultList();

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

			em.persist(connection);
		});
	}

	@Override
	@Transactional
	public String getProcessModel(Long id) {

		return em.find(ProcessModelTable.class, id).getBpmnXml();
	}

	@Override
	@Transactional
	public List<ProcessInformation> getProcessInformation() {
		return em//
				.createQuery("SELECT pm FROM ProcessModelTable pm", ProcessModelTable.class)//
				.getResultList()//
				.stream()//
				.map(model -> new ProcessInformation(//
						model.getId(), //
						model.getName(), //
						model.getDescription(), //
						model.getCreatedAt()))//
				.collect(Collectors.toList());
	}

	@Transactional
	public List<ProcessConnection> getProcessConnections() {
		return em//
				.createQuery("SELECT pc FROM ProcessConnectionTable pc", ProcessConnectionTable.class)//
				.getResultList()//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	@Transactional
	public List<DataStore> getDataStores() {
		return em//
				.createQuery("SELECT d FROM DataStoreTable d", DataStoreTable.class)//
				.getResultList()//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	@Transactional
	public List<DataStoreConnection> getDataStoreConnections() {
		return em//
				.createQuery("SELECT dc FROM DataStoreConnectionTable dc", DataStoreConnectionTable.class)//
				.getResultList()//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	private ProcessConnection map(ProcessConnectionTable table) {
		ProcessConnection connection = new ProcessConnection();
		connection.setCallingProcessid(table.getCallingProcess().getId());
		connection.setCallingElementType(table.getCallingElementType());

		connection.setCalledProcessid(table.getCalledProcess().getId());
		connection.setCalledElementType(table.getCalledElementType());
		return connection;
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

	@Override
	public ProcessDetails getProcessDetails(Long id) {
		ProcessModelTable table = em.find(ProcessModelTable.class, id);

		ProcessDetails details = new ProcessDetails();
		details.setId(table.getId());
		details.setName(table.getName());
		details.setDescription(table.getDescription());
		details.setStartEvents(map(table.getEvents(), EventType.START));
		details.setIntermediateCatchEvents(map(table.getEvents(), EventType.INTERMEDIATE_CATCH));
		details.setIntermediateThrowEvents(map(table.getEvents(), EventType.INTERMEDIATE_THROW));
		details.setEndEvents(map(table.getEvents(), EventType.END));

		return details;
	}

	private List<ProcessEvent> map(List<ProcessEventTable> events, EventType eventType) {
		return events//
				.stream()//
				.filter(event -> event.getEventType().equals(eventType))//
				.map(event -> map(event))//
				.collect(Collectors.toList());
	}

	private ProcessEvent map(ProcessEventTable table) {
		ProcessEvent event = new ProcessEvent();
		event.setElementId(table.getElementId());
		event.setLabel(table.getLabel());
		return event;
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
}