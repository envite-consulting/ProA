package de.envite.proa.repository.processmodel;

import de.envite.proa.XmlConverter;
import de.envite.proa.entities.process.*;
import de.envite.proa.repository.tables.*;
import de.envite.proa.usecases.processmodel.RelatedProcessModelRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProcessmodelMapper {

	public static ProcessModelTable map(ProcessModel processModel, ProjectTable projectTable) {
		ProcessModelTable table = new ProcessModelTable();
		table.setName(processModel.getName());

		table.setEvents(mapEvents(processModel.getEvents(), table, projectTable));
		table.setCallActivites(map(processModel.getCallActivities(), table, projectTable));
		table.setDataStores(mapDataStore(processModel.getDataStores(), table));
		table.setDescription(processModel.getDescription());
		table.setProject(projectTable);
		table.setBpmnProcessId(processModel.getBpmnProcessId());
		table.setBpmnXml(XmlConverter.stringToBytes(processModel.getBpmnXml()));
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
				.toList();
	}

	private static Set<CallActivityTable> map(Set<ProcessActivity> callActivities, ProcessModelTable processModelTable,
			ProjectTable projectTable) {
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
				.collect(Collectors.toSet());
	}

	private static Set<ProcessEventTable> mapEvents(Set<ProcessEvent> events, ProcessModelTable processModelTable,
			ProjectTable projectTable) {
		return events//
				.stream()//
				.map(event -> map(event, event.getEventType(), processModelTable, projectTable))//
				.collect(Collectors.toSet());
	}

	private static ProcessEventTable map(ProcessEvent event, EventType eventType, ProcessModelTable processModelTable,
			ProjectTable projectTable) {
		ProcessEventTable processEventtable = new ProcessEventTable();
		processEventtable.setElementId(event.getElementId());
		processEventtable.setLabel(event.getLabel());
		processEventtable.setEventType(eventType);
		processEventtable.setProcessModel(processModelTable);
		processEventtable.setProject(projectTable);
		return processEventtable;
	}

	public static ProcessInformation map(ProcessModelTable table, //
			RelatedProcessModelDao relatedProcessModelDao, //
			RelatedProcessModelRepository relatedProcessModelRepository) {
		ProcessInformation processInformation = new ProcessInformation();
		processInformation.setId(table.getId());
		processInformation.setBpmnProcessId(table.getBpmnProcessId());
		processInformation.setProcessName(table.getName());
		processInformation.setDescription(table.getDescription());
		processInformation.setCreatedAt(table.getCreatedAt());
		processInformation.setLevel(table.getLevel());
		processInformation.setChildrenIds(table.getChildren()
				.stream()
				.map(ProcessModelTable::getId)
				.toList());
		processInformation.setProcessType(table.getProcessType());
		processInformation.setRelatedProcessModels(relatedProcessModelDao.getRelatedProcessModels(table)
				.stream()
				.map(relatedProcessModelRepository::mapToRelatedProcessModel)
				.toList());

		return processInformation;
	}
}