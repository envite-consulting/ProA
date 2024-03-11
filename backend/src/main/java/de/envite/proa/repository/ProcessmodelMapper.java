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

public class ProcessmodelMapper {

	public static ProcessModelTable map(ProcessModel processModel) {
		ProcessModelTable table = new ProcessModelTable();
		table.setName(processModel.getName());
		table.setBpmnXml(processModel.getBpmnXml());
		table.setEvents(mapEvents(processModel.getEvents(), table));
		table.setCallActivites(map(processModel.getCallActivities(), table));
		table.setDataStores(mapDataStore(processModel.getDataStores(), table));
		table.setDescription(processModel.getDescription());
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

	private static List<ProcessEventTable> mapEvents(List<ProcessEvent> events, ProcessModelTable processModelTable) {
		return events//
				.stream()//
				.map(event -> map(event, event.getEventType(), processModelTable))//
				.collect(Collectors.toList());
	}

	private static ProcessEventTable map(ProcessEvent event, EventType eventType, ProcessModelTable processModelTable) {
		ProcessEventTable processEventtable = new ProcessEventTable();
		processEventtable.setElementId(event.getElementId());
		processEventtable.setLabel(event.getLabel());
		processEventtable.setEventType(eventType);
		processEventtable.setProcessModel(processModelTable);
		return processEventtable;
	}
}