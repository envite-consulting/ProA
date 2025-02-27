package de.envite.proa.repository.processmodel;

import de.envite.proa.entities.process.EventType;
import de.envite.proa.entities.process.ProcessActivity;
import de.envite.proa.entities.process.ProcessDetails;
import de.envite.proa.entities.process.ProcessEvent;
import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import jakarta.persistence.NoResultException;

import java.util.Set;
import java.util.stream.Collectors;

public class ProcessDetailsMapper {

	public static ProcessDetails map(ProcessModelTable table) {
		ProcessDetails details = new ProcessDetails();
		details.setId(table.getId());
		details.setName(table.getName());
		details.setDescription(table.getDescription());
		details.setBpmnProcessId(table.getBpmnProcessId());
		details.setStartEvents(map(table.getEvents(), EventType.START));
		details.setIntermediateCatchEvents(map(table.getEvents(), EventType.INTERMEDIATE_CATCH));
		details.setIntermediateThrowEvents(map(table.getEvents(), EventType.INTERMEDIATE_THROW));
		details.setEndEvents(map(table.getEvents(), EventType.END));
		details.setActivities(map(table.getCallActivites()));
		return details;
	}

	public static ProcessDetails mapWithAggregation(Long id,
													Set<ProcessModelTable> processModels,
													Set<ProcessEventTable> events,
													Set<CallActivityTable> activities) {
		ProcessModelTable mainProcess = processModels.stream()
				.filter(p -> p.getId().equals(id))
				.findFirst()
				.orElseThrow(() -> new NoResultException("Process model not found"));

		ProcessDetails details = new ProcessDetails();
		details.setId(mainProcess.getId());
		details.setName(mainProcess.getName());
		details.setDescription(mainProcess.getDescription());
		details.setBpmnProcessId(mainProcess.getBpmnProcessId());
		details.setStartEvents(map(events, EventType.START));
		details.setIntermediateCatchEvents(map(events, EventType.INTERMEDIATE_CATCH));
		details.setIntermediateThrowEvents(map(events, EventType.INTERMEDIATE_THROW));
		details.setEndEvents(map(events, EventType.END));
		details.setActivities(map(activities));
		return details;
	}

	private static Set<ProcessActivity> map(Set<CallActivityTable> callActivites) {
		return callActivites//
				.stream()//
				.map(activity -> new ProcessActivity(activity.getElementId(), activity.getLabel()))//
				.collect(Collectors.toSet());
	}

	private static Set<ProcessEvent> map(Set<ProcessEventTable> events, EventType eventType) {
		return events//
				.stream()//
				.filter(event -> event.getEventType().equals(eventType))//
				.map(ProcessDetailsMapper::map)//
				.collect(Collectors.toSet());
	}

	private static ProcessEvent map(ProcessEventTable table) {
		ProcessEvent event = new ProcessEvent();
		event.setElementId(table.getElementId());
		event.setLabel(table.getLabel());
		event.setEventType(table.getEventType());
		return event;
	}
}
