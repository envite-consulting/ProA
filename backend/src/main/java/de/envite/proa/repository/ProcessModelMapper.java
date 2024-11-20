package de.envite.proa.repository;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.EventType;
import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessDataStore;
import de.envite.proa.entities.ProcessEvent;
import de.envite.proa.entities.ProcessModel;
import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.ProcessDataStoreTable;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;

public class ProcessModelMapper {

	public static ProcessModelTable map(ProcessModel processModel, ProjectTable projectTable) {
		ProcessModelTable table = new ProcessModelTable();
		table.setName(processModel.getName());
		table.setBpmnXml(processModel.getBpmnXml());
		table.setEvents(mapEvents(processModel.getEvents(), table, projectTable));
		table.setCallActivites(map(processModel.getCallActivities(), table, projectTable));
		table.setDataStores(mapDataStore(processModel.getDataStores(), table));
		table.setDescription(processModel.getDescription());
		table.setProject(projectTable);
		table.setBpmnProcessId(processModel.getBpmnProcessId());
		table.setProcessType(processModel.getProcessType());
		return table;
	}

	private static List<ProcessDataStoreTable> mapDataStore(List<ProcessDataStore> dataStores,
			ProcessModelTable table) {
		return dataStores//
				.stream()//
				.map(store -> {
					ProcessDataStoreTable dataStoreTable = new ProcessDataStoreTable();
					dataStoreTable.setElementId(store.getElementId());
					dataStoreTable.setLabel(store.getLabel());
					dataStoreTable.setAccess(store.getAccess());
					dataStoreTable.setProcessModel(table);
					return dataStoreTable;
				})//
				.collect(Collectors.toList());
	}

	private static List<CallActivityTable> map(List<ProcessActivity> callActivities,
			ProcessModelTable processModelTable, ProjectTable projectTable) {
		return callActivities//
				.stream()//
				.map(activity -> {
					CallActivityTable callActivityTable = new CallActivityTable();
					callActivityTable.setElementId(activity.getElementId());
					callActivityTable.setLabel(activity.getLabel());
					callActivityTable.setProcessModel(processModelTable);
					callActivityTable.setProject(projectTable);
					return callActivityTable;
				})//
				.collect(Collectors.toList());
	}

	private static List<ProcessEventTable> mapEvents(List<ProcessEvent> events, ProcessModelTable processModelTable, ProjectTable projectTable) {
		return events//
				.stream()//
				.map(event -> map(event, event.getEventType(), processModelTable, projectTable))//
				.collect(Collectors.toList());
	}

	private static ProcessEventTable map(ProcessEvent event, EventType eventType, ProcessModelTable processModelTable, ProjectTable projectTable) {
		ProcessEventTable processEventtable = new ProcessEventTable();
		processEventtable.setElementId(event.getElementId());
		processEventtable.setLabel(event.getLabel());
		processEventtable.setEventType(eventType);
		processEventtable.setProcessModel(processModelTable);
		processEventtable.setProject(projectTable);
		return processEventtable;
	}
}
