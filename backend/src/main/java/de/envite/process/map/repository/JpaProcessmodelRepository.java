package de.envite.process.map.repository;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.process.map.entities.ProcessConnection;
import de.envite.process.map.entities.ProcessEvent;
import de.envite.process.map.entities.ProcessInformation;
import de.envite.process.map.entities.ProcessMap;
import de.envite.process.map.entities.ProcessModel;
import de.envite.process.map.repository.tables.EventType;
import de.envite.process.map.repository.tables.ProcessConnectionTable;
import de.envite.process.map.repository.tables.ProcessEventTable;
import de.envite.process.map.repository.tables.ProcessModelTable;
import de.envite.process.map.usecases.ProcessModelRepository;
import de.envite.process.map.usecases.processmap.ProcessMapRespository;
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
	
	@Transactional
	public List<ProcessConnection> getProcessConnections(){
		return em//
				.createQuery("SELECT pc FROM ProcessConnectionTable pc", ProcessConnectionTable.class)//
				.getResultList()//
				.stream()//
				.map(connection -> new ProcessConnection(
						connection.getCallingProcess().getId(),
						connection.getCalledProcess().getId()))//
				.collect(Collectors.toList());
	}

	@Override
	public ProcessMap getProcessMap() {
		
		List<ProcessInformation> processModelInformation = getProcessModels();
		List<ProcessConnection> processConnections = getProcessConnections();
		
		ProcessMap map = new ProcessMap();
		map.setConnections(processConnections);
		map.setProcesses(processModelInformation);
		
		return map;
	}
}