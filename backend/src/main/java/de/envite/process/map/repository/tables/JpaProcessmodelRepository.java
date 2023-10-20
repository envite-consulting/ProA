package de.envite.process.map.repository.tables;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.process.map.entities.ProcessEvent;
import de.envite.process.map.entities.ProcessInformation;
import de.envite.process.map.entities.ProcessModel;
import de.envite.process.map.usecases.ProcesModelRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class JpaProcessmodelRepository implements ProcesModelRepository {

	@Inject
	private EntityManager em;

	@Override
	@Transactional
	public Long saveProcessModel(ProcessModel processModel) {
		ProcessModelTable table = map(processModel);
		em.persist(table);

		processModel//
				.getEndEvents()//
				.forEach(event -> {
					connectWithStartEvents(table, event);
				});

		em.flush();
		return table.getId();
	}

	private void connectWithStartEvents(ProcessModelTable newTable, ProcessEvent newEvent) {
		List<ProcessEventTable> startEventsWithSameLabel = em
				.createQuery("SELECT e FROM ProcessEventTable e WHERE e.label = :label AND e.eventType=:eventType",
						ProcessEventTable.class)
				.setParameter("label", newEvent.getLabel())//
				.setParameter("eventType", EventType.START)//
				.getResultList();
		
		
		startEventsWithSameLabel.forEach(event->{
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(newTable);
			connection.setCalledProcess(event.getProcessModel());
			connection.setCallingElementType(EventType.START.toString());
			connection.setCallingElement(newEvent.getElementId());
			connection.setCalledElementType(EventType.END.toString());
			connection.setCalledElement(event.getElementId());
			
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
	public List<ProcessInformation> getProcessModels() {
		return em//
				.createQuery("SELECT pm FROM ProcessModelTable pm", ProcessModelTable.class)//
				.getResultList()//
				.stream()//
				.map(model -> new ProcessInformation(model.getId(), model.getName()))//
				.collect(Collectors.toList());
	}

	private ProcessModelTable map(ProcessModel processModel) {
		ProcessModelTable table = new ProcessModelTable();
		table.setName(processModel.getName());
		table.setBpmnXml(processModel.getBpmnXml());
		table.setStartEvents(map(processModel.getStartEvents(), EventType.START, table));
		table.setIntermediateEvents(map(processModel.getIntermediateEvents(), EventType.INTERMEDIATE,table));
		table.setEndEvents(map(processModel.getEndEvents(), EventType.END,table));

		return table;
	}

	private List<ProcessEventTable> map(List<ProcessEvent> events, EventType eventType, ProcessModelTable processModelTable) {
		return events//
				.stream()//
				.map(event -> map(event, eventType, processModelTable))//
				.collect(Collectors.toList());
	}

	private ProcessEventTable map(ProcessEvent event, EventType eventType, ProcessModelTable processModelTable) {
		ProcessEventTable table = new ProcessEventTable();
		table.setElementId(event.getElementId());
		table.setLabel(event.getLabel());
		table.setEventType(eventType);
		table.setProcessModel(processModelTable);
		return table;
	}
}