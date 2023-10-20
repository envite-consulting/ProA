package de.envite.process.map.repository;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.process.map.entities.ProcessEvent;
import de.envite.process.map.entities.ProcessModel;
import de.envite.process.map.repository.tables.EventType;
import de.envite.process.map.repository.tables.ProcessEventTable;
import de.envite.process.map.repository.tables.ProcessModelTable;

public class ProcessmodelMapper {
	
	public static ProcessModelTable map(ProcessModel processModel) {
		ProcessModelTable table = new ProcessModelTable();
		table.setName(processModel.getName());
		table.setBpmnXml(processModel.getBpmnXml());
		table.setStartEvents(map(processModel.getStartEvents(), EventType.START, table));
		table.setIntermediateEvents(map(processModel.getIntermediateEvents(), EventType.INTERMEDIATE,table));
		table.setEndEvents(map(processModel.getEndEvents(), EventType.END,table));

		return table;
	}

	private static List<ProcessEventTable> map(List<ProcessEvent> events, EventType eventType, ProcessModelTable processModelTable) {
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
		table.setProcessModel(processModelTable);
		return table;
	}
}