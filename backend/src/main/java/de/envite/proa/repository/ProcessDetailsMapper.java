package de.envite.proa.repository;

import java.util.List;

import de.envite.proa.entities.EventType;
import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessEvent;
import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import jakarta.persistence.NoResultException;

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

	public static ProcessDetails mapWithAggregation(Long id, //
													List<ProcessModelTable> processModels, //
													List<ProcessEventTable> events, //
													List<CallActivityTable> activities) {
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

	private static List<ProcessActivity> map(List<CallActivityTable> callActivities) {
		return callActivities//
				.stream()//
				.map(activity -> new ProcessActivity(activity.getElementId(), activity.getLabel()))//
				.toList();
	}

	private static List<ProcessEvent> map(List<ProcessEventTable> events, EventType eventType) {
		return events//
				.stream()//
				.filter(event -> event.getEventType().equals(eventType))//
				.map(ProcessDetailsMapper::map)//
				.toList();
	}

	private static ProcessEvent map(ProcessEventTable table) {
		ProcessEvent event = new ProcessEvent();
		event.setElementId(table.getElementId());
		event.setLabel(table.getLabel());
		event.setEventType(table.getEventType());
		return event;
	}
}
