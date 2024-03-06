package de.envite.proa.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessElementType;
import de.envite.proa.entities.ProcessEvent;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.entities.ProcessMap;
import de.envite.proa.entities.ProcessModel;
import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.EventType;
import de.envite.proa.repository.tables.ProcessConnectionTable;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.usecases.ProcessModelRepository;
import de.envite.proa.usecases.processmap.ProcessMapRespository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class JpaProcessmodelRepository implements ProcessModelRepository, ProcessMapRespository {

	@Inject
	private EntityManager em;

	@Override
	@Transactional
	public Long saveProcessModel(ProcessModel processModel) {

		ProcessModelTable table = ProcessmodelMapper.map(processModel);
		table.setCreatedAt(LocalDateTime.now());
		em.persist(table);

		connectEvents(processModel, table);

		connectCallActivities(processModel, table);

		em.flush();
		return table.getId();
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
				.getEndEvents()//
				.forEach(event -> {
					connectWithStartEvents(table, event);
				});
		processModel//
				.getStartEvents()//
				.forEach(event -> {
					connectWithEndEvents(table, event);
				});
	}

	private void connectWithStartEvents(ProcessModelTable newTable, ProcessEvent newEndEvent) {
		List<ProcessEventTable> startEventsWithSameLabel = em
				.createQuery("SELECT e FROM ProcessEventTable e WHERE e.label = :label AND e.eventType=:eventType",
						ProcessEventTable.class)
				.setParameter("label", newEndEvent.getLabel())//
				.setParameter("eventType", EventType.START)//
				.getResultList();

		startEventsWithSameLabel.forEach(event -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(newTable);
			connection.setCallingElement(newEndEvent.getElementId());
			connection.setCallingElementType(ProcessElementType.END_EVENT);
			connection.setCalledProcess(event.getProcessModelForStartEvent());
			connection.setCalledElement(event.getElementId());
			connection.setCalledElementType(ProcessElementType.START_EVENT);

			System.out.println(connection.toString());
			em.persist(connection);
		});
	}

	private void connectWithEndEvents(ProcessModelTable newTable, ProcessEvent newEvent) {
		List<ProcessEventTable> endEventsWithSameLabel = em
				.createQuery("SELECT e FROM ProcessEventTable e WHERE e.label = :label AND e.eventType=:eventType",
						ProcessEventTable.class)
				.setParameter("label", newEvent.getLabel())//
				.setParameter("eventType", EventType.END)//
				.getResultList();

		endEventsWithSameLabel.forEach(event -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(event.getProcessModelForEndEvent());
			connection.setCallingElement(event.getElementId());
			connection.setCallingElementType(ProcessElementType.END_EVENT);
			connection.setCalledProcess(newTable);
			connection.setCalledElement(newEvent.getElementId());
			connection.setCalledElementType(ProcessElementType.START_EVENT);

			System.out.println(connection.toString());
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

		ProcessMap map = new ProcessMap();
		map.setConnections(processConnections);
		map.setProcesses(processModelInformation);

		return map;
	}

	@Override
	public ProcessDetails getProcessDetails(Long id) {
		ProcessModelTable table = em.find(ProcessModelTable.class, id);

		ProcessDetails details = new ProcessDetails();
		details.setId(table.getId());
		details.setName(table.getName());
		details.setDescription(table.getDescription());
		details.setStartEvents(map(table.getStartEvents()));
		details.setIntermediateEvents(map(table.getIntermediateEvents()));
		details.setEndEvents(map(table.getEndEvents()));

		return details;
	}

	private List<ProcessEvent> map(List<ProcessEventTable> events) {
		return events//
				.stream()//
				.map(event -> map(event))//
				.collect(Collectors.toList());
	}

	private ProcessEvent map(ProcessEventTable table) {
		ProcessEvent event = new ProcessEvent();
		event.setElementId(table.getElementId());
		event.setLabel(table.getLabel());
		return event;
	}
}