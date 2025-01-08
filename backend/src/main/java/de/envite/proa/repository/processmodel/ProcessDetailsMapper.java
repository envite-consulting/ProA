package de.envite.proa.repository.processmodel;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.process.EventType;
import de.envite.proa.entities.process.ProcessActivity;
import de.envite.proa.entities.process.ProcessDetails;
import de.envite.proa.entities.process.ProcessEvent;
import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;

public class ProcessDetailsMapper {

	public static ProcessDetails map(ProcessModelTable table) {
		ProcessDetails details = new ProcessDetails();
		details.setId(table.getId());
		details.setName(table.getName());
		details.setDescription(table.getDescription());
		details.setStartEvents(map(table.getEvents(), EventType.START));
		details.setIntermediateCatchEvents(map(table.getEvents(), EventType.INTERMEDIATE_CATCH));
		details.setIntermediateThrowEvents(map(table.getEvents(), EventType.INTERMEDIATE_THROW));
		details.setEndEvents(map(table.getEvents(), EventType.END));
		details.setActivities(map(table.getCallActivites()));
		details.setBpmnProcessId(table.getBpmnProcessId());
		return details;
	}

	private static List<ProcessActivity> map(List<CallActivityTable> callActivites) {
		return callActivites//
				.stream()//
				.map(activity -> new ProcessActivity(activity.getElementId(), activity.getLabel()))//
				.collect(Collectors.toList());
	}

	private static List<ProcessEvent> map(List<ProcessEventTable> events, EventType eventType) {
		return events//
				.stream()//
				.filter(event -> event.getEventType().equals(eventType))//
				.map(event -> map(event))//
				.collect(Collectors.toList());
	}

	private static ProcessEvent map(ProcessEventTable table) {
		ProcessEvent event = new ProcessEvent();
		event.setElementId(table.getElementId());
		event.setLabel(table.getLabel());
		event.setEventType(table.getEventType());
		return event;
	}
}