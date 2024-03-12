package de.envite.proa.repository;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.EventType;
import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessEvent;
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
		return details;
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
		return event;
	}
}