package de.envite.proa.repository;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessEvent;
import de.envite.proa.entities.ProcessModel;
import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.EventType;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;

public class ProcessmodelMapper {

	public static ProcessModelTable map(ProcessModel processModel) {
		ProcessModelTable table = new ProcessModelTable();
		table.setName(processModel.getName());
		table.setBpmnXml(processModel.getBpmnXml());
		table.setStartEvents(map(processModel.getStartEvents(), EventType.START, table));
		table.setIntermediateEvents(map(processModel.getIntermediateEvents(), EventType.INTERMEDIATE, table));
		table.setEndEvents(map(processModel.getEndEvents(), EventType.END, table));
		table.setCallActivites(map(processModel.getCallActivities(), table));
		table.setDescription(processModel.getDescription());
		return table;
	}

	private static List<CallActivityTable> map(List<ProcessActivity> callActivities,
			ProcessModelTable processModelTable) {
		return callActivities//
				.stream()//
				.map(activity -> {
					CallActivityTable callActivityTable = new CallActivityTable();
					callActivityTable.setElementId(activity.getElementId());
					callActivityTable.setLabel(activity.getLabel());
					callActivityTable.setProcessModel(processModelTable);
					return callActivityTable;
				})//
				.collect(Collectors.toList());
	}

	private static List<ProcessEventTable> map(List<ProcessEvent> events, EventType eventType,
			ProcessModelTable processModelTable) {
		return events//
				.stream()//
				.map(event -> map(event, eventType, processModelTable))//
				.collect(Collectors.toList());
	}

	private static ProcessEventTable map(ProcessEvent event, EventType eventType, ProcessModelTable processModelTable) {
		ProcessEventTable table = new ProcessEventTable();
		table.setElementId(event.getElementId());
		table.setLabel(event.getLabel());
		table.setEventType(eventType);
		if (eventType.equals(EventType.START)) {
			table.setProcessModelForStartEvent(processModelTable);
		} else if (eventType.equals(EventType.INTERMEDIATE)) {
			table.setProcessModelForIntermediateEvent(processModelTable);
		} else if (eventType.equals(EventType.END)) {
			table.setProcessModelForEndEvent(processModelTable);
		}
		return table;
	}
}