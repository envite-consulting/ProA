package de.envite.proa.repository.processmodel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.envite.proa.XmlConverter;
import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.process.*;
import de.envite.proa.repository.datastore.DataStoreConnectionDao;
import de.envite.proa.repository.datastore.DataStoreDao;
import de.envite.proa.repository.messageflow.MessageFlowDao;
import de.envite.proa.repository.messageflow.MessageFlowMapper;
import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.DataStoreConnectionTable;
import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProcessConnectionTable;
import de.envite.proa.repository.tables.ProcessDataStoreTable;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.usecases.RelatedProcessModelRepository;
import de.envite.proa.usecases.processmodel.ProcessModelRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

@RequestScoped
public class ProcessmodelRepositoryImpl implements ProcessModelRepository {

    private final ProcessModelDao processModelDao;
    private final DataStoreDao dataStoreDao;
    private final DataStoreConnectionDao dataStoreConnectionDao;
    private final CallActivityDao callActivityDao;
    private final ProcessConnectionDao processConnectionDao;
    private final ProcessEventDao processEventDao;
    private final MessageFlowDao messageFlowDao;
    private final RelatedProcessModelDao relatedProcessModelDao;
    private final RelatedProcessModelRepository relatedProcessModelRepository;

    @Inject
    public ProcessmodelRepositoryImpl(ProcessModelDao processModelDao, //
                                      DataStoreDao dataStoreDao, //
                                      DataStoreConnectionDao dataStoreConnectionDao, //
                                      CallActivityDao callActivityDao, //
                                      ProcessConnectionDao processConnectionDao, //
                                      ProcessEventDao processEventDao, //
                                      MessageFlowDao messageFlowDao, //
                                      RelatedProcessModelDao relatedProcessModelDao, //
                                      RelatedProcessModelRepository relatedProcessModelRepository) {
        this.processModelDao = processModelDao;
        this.dataStoreDao = dataStoreDao;
        this.dataStoreConnectionDao = dataStoreConnectionDao;
        this.callActivityDao = callActivityDao;
        this.processConnectionDao = processConnectionDao;
        this.processEventDao = processEventDao;
        this.messageFlowDao = messageFlowDao;
        this.relatedProcessModelDao = relatedProcessModelDao;
        this.relatedProcessModelRepository = relatedProcessModelRepository;
    }

    @Override
    public Long saveProcessModel(Long projectId, ProcessModel processModel) {
        ProjectTable projectTable = new ProjectTable();
        projectTable.setId(projectId);

        ProcessModelTable table = ProcessmodelMapper.map(processModel, projectTable);
        table.setCreatedAt(LocalDateTime.now());
        table.setProject(projectTable);

        String parentBpmnProcessId = processModel.getParentBpmnProcessId();
        processModelDao.persist(table);
        if (parentBpmnProcessId != null) {
            ProcessModelTable parent = processModelDao.findByBpmnProcessIdWithChildren(parentBpmnProcessId, projectTable);
            parent.getChildren().add(table);
            processModelDao.merge(parent);
        }

        relatedProcessModelRepository.calculateAndSaveRelatedProcessModels(projectTable);

        connectEvents(processModel, table, projectTable);

        connectCallActivities(processModel, table, projectTable);

        connectDataStores(table, projectTable);

        return table.getId();
    }

    @Override
    public void addRelatedProcessModel(Long projectId, Long id, List<Long> relatedProcessModelIds) {
        relatedProcessModelRepository.addRelatedProcessModel(projectId, id, relatedProcessModelIds);
    }

    @Override
    public List<ProcessInformation> getProcessInformation(Long projectId, String levelParam) {
        ProjectTable projectTable = new ProjectTable();
        projectTable.setId(projectId);

        List <Integer> levels = (levelParam == null || levelParam.isEmpty())
                ? null : Arrays.stream(levelParam.split(","))
                .map(Integer::parseInt)
                .toList();

        return processModelDao.getProcessModels(projectTable, levels).stream()
                .map(model -> new ProcessInformation(
                        model.getId(), //
                        model.getBpmnProcessId(), //
                        model.getName(), //
                        model.getDescription(), //
                        model.getCreatedAt(), //
                        model.getLevel(), //
                        model.getParents().stream().map(ProcessModelTable::getBpmnProcessId).toList(), //
                        model.getChildren().stream().map(ProcessModelTable::getBpmnProcessId).toList(), //
                        relatedProcessModelDao.getRelatedProcessModels(model).stream().map(relatedProcessModelRepository::mapToRelatedProcessModel).toList()
                ))
                .toList();
    }

    @Override
    public ProcessInformation getProcessInformationById(Long projectId, Long id) {
        ProjectTable projectTable = new ProjectTable();
        projectTable.setId(projectId);
        ProcessModelTable model = processModelDao.getProcessModelById(projectTable, id);

        return new ProcessInformation(
                model.getId(), //
                model.getBpmnProcessId(), //
                model.getName(), //
                model.getDescription(), //
                model.getCreatedAt(), //
                model.getLevel(), //
                model.getParents().stream()
                        .map(ProcessModelTable::getBpmnProcessId)
                        .toList(), //
                model.getChildren().stream()
                        .map(ProcessModelTable::getBpmnProcessId)
                        .toList(), //
                relatedProcessModelDao.getRelatedProcessModels(model).stream()
                        .map(relatedProcessModelRepository::mapToRelatedProcessModel)
                        .toList()
        );
    }

    @Override
    public ProcessDetails getProcessDetails(Long id, boolean aggregate) {
        if (aggregate) {
            List<ProcessModelTable> relatedProcessModels = processModelDao.findWithRelatedProcessModels(id);
            List<ProcessEventTable> allEvents = processModelDao.findEventsForProcessModels(relatedProcessModels);
            List<CallActivityTable> allCallActivities = processModelDao.findCallActivitiesForProcessModels(relatedProcessModels);

            return ProcessDetailsMapper.mapWithAggregation(id, relatedProcessModels, allEvents, allCallActivities);
        } else {
            ProcessModelTable table = processModelDao.findWithChildren(id);

            return ProcessDetailsMapper.map(table);
        }
    }

    @Override
    public void saveMessageFlows(List<MessageFlowDetails> messageFlows, Long projectId) {
        ProjectTable projectTable = new ProjectTable();
        projectTable.setId(projectId);

        messageFlows.forEach(messageFlow ->
            messageFlowDao.persist(MessageFlowMapper.map(messageFlow, projectTable)));
    }

    private void connectDataStores(ProcessModelTable table, ProjectTable projectTable) {
        table//
                .getDataStores()//
                .forEach(store -> connectProcessDataStore(store, table, projectTable));

    }

    private void connectProcessDataStore(ProcessDataStoreTable store, ProcessModelTable table,
                                         ProjectTable projectTable) {
        DataStoreTable dataStoreTable;
        try {

            dataStoreTable = dataStoreDao.getDataStoreForLabel(store.getLabel(), projectTable);
        } catch (NoResultException e) {
            dataStoreTable = new DataStoreTable();
            dataStoreTable.setLabel(store.getLabel());
            dataStoreTable.setProject(projectTable);
            dataStoreDao.persist(dataStoreTable);
        }

        DataStoreConnectionTable connectionTable = new DataStoreConnectionTable();
        connectionTable.setAccess(store.getAccess());
        connectionTable.setProcess(table);
        connectionTable.setDataStore(dataStoreTable);
        connectionTable.setProject(projectTable);

        dataStoreConnectionDao.persist(connectionTable);
    }

    private void connectCallActivities(ProcessModel processModel, ProcessModelTable table, ProjectTable projectTable) {
        processModel//
                .getCallActivities()//
                .forEach(activity ->
                    connectCallActivityWithProcess(table, activity, projectTable));

        connectProcessWithCallActivity(table, projectTable);
    }

    private void connectProcessWithCallActivity(ProcessModelTable table, ProjectTable projectTable) {
        List<CallActivityTable> callActivityTables = callActivityDao.getCallActivitiesForName(table.getName(),
                projectTable);

        callActivityTables.forEach(callActivityTable -> {
            ProcessConnectionTable connection = new ProcessConnectionTable();
            connection.setCallingProcess(callActivityTable.getProcessModel());
            connection.setCallingElementType(ProcessElementType.CALL_ACTIVITY);
            connection.setCallingElement(callActivityTable.getElementId());

            connection.setCalledProcess(table);
            connection.setCalledElementType(ProcessElementType.START_EVENT);
            // called element remains empty
            connection.setLabel(callActivityTable.getLabel());
            connection.setProject(projectTable);
            connection.setUserCreated(false);
            processConnectionDao.persist(connection);
        });
    }

    private void connectCallActivityWithProcess(ProcessModelTable table, ProcessActivity activity,
                                                ProjectTable projectTable) {
        List<ProcessModelTable> processModelTable = processModelDao.getProcessModelsForName(activity.getLabel(),
                projectTable);

        processModelTable.forEach(process -> {
            ProcessConnectionTable connection = new ProcessConnectionTable();
            connection.setCallingProcess(table);
            connection.setCallingElement(activity.getElementId());
            connection.setCallingElementType(ProcessElementType.CALL_ACTIVITY);
            connection.setCalledProcess(process);
            // When the entire process is called, the called process is started using the
            // start event
            connection.setCalledElementType(ProcessElementType.START_EVENT);
            // called element remains empty
            connection.setLabel(process.getName());
            connection.setProject(projectTable);
            connection.setUserCreated(false);
            processConnectionDao.persist(connection);
        });
    }

    private void connectEvents(ProcessModel processModel, ProcessModelTable table, ProjectTable projectTable) {
        processModel//
                .getEvents()//
                .forEach(event -> connectEvents(table, event, projectTable));
    }

    private void connectEvents(ProcessModelTable table, ProcessEvent event, ProjectTable projectTable) {
        if (event.getEventType() == EventType.START || event.getEventType() == EventType.INTERMEDIATE_CATCH) {
            connectWithThrowEvents(table, event, EventType.INTERMEDIATE_THROW, projectTable);
            connectWithThrowEvents(table, event, EventType.END, projectTable);
        } else if (event.getEventType() == EventType.INTERMEDIATE_THROW || event.getEventType() == EventType.END) {
            connectWithCatchEvents(table, event, EventType.START, projectTable);
            connectWithCatchEvents(table, event, EventType.INTERMEDIATE_CATCH, projectTable);
        } else {
            throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
        }
    }

    private void connectWithCatchEvents(ProcessModelTable newTable, ProcessEvent newThrowEvent,
                                        EventType eventTypeToConnectTo, ProjectTable projectTable) {
        List<ProcessEventTable> startEventsWithSameLabel = processEventDao
                .getEventsForLabelAndType(newThrowEvent.getLabel(), eventTypeToConnectTo, projectTable);

		startEventsWithSameLabel.forEach(event -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(newTable);
			connection.setCallingElement(newThrowEvent.getElementId());
			if (newThrowEvent.getEventType().equals(EventType.INTERMEDIATE_THROW)) {
				connection.setCallingElementType(ProcessElementType.INTERMEDIATE_THROW_EVENT);
			} else {
				connection.setCallingElementType(ProcessElementType.END_EVENT);
			}

			connection.setCalledProcess(event.getProcessModel());
			connection.setCalledElement(event.getElementId());
			if (eventTypeToConnectTo.equals(EventType.START)) {
				connection.setCalledElementType(ProcessElementType.START_EVENT);
			} else {
				connection.setCalledElementType(ProcessElementType.INTERMEDIATE_CATCH_EVENT);
			}

            connection.setLabel(event.getLabel());
            connection.setProject(projectTable);
            connection.setUserCreated(false);
            processConnectionDao.persist(connection);
        });
    }

    private void connectWithThrowEvents(ProcessModelTable newTable, ProcessEvent newEvent,
                                        EventType eventTypeForConnectionFrom, ProjectTable projectTable) {

        List<ProcessEventTable> endEventsWithSameLabel = processEventDao.getEventsForLabelAndType(newEvent.getLabel(),
                eventTypeForConnectionFrom, projectTable);

		endEventsWithSameLabel.forEach(event -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(event.getProcessModel());
			connection.setCallingElement(event.getElementId());
			if (eventTypeForConnectionFrom.equals(EventType.END)) {
				connection.setCallingElementType(ProcessElementType.END_EVENT);
			} else {
				connection.setCallingElementType(ProcessElementType.INTERMEDIATE_THROW_EVENT);
			}

			connection.setCalledProcess(newTable);
			connection.setCalledElement(newEvent.getElementId());
			if (newEvent.getEventType().equals(EventType.START)) {
				connection.setCalledElementType(ProcessElementType.START_EVENT);
			} else {
				connection.setCalledElementType(ProcessElementType.INTERMEDIATE_CATCH_EVENT);
			}

            connection.setLabel(event.getLabel());
            connection.setProject(projectTable);
            connection.setUserCreated(false);
            processConnectionDao.persist(connection);
        });
    }

    @Override
    public ProcessModelTable getProcessModel(Long id) {
        return processModelDao.find(id);
    }

    @Override
    public String getProcessModelXml(Long id) {
        byte[] xmlBytes = processModelDao.find(id).getBpmnXml();
        return XmlConverter.bytesToString(xmlBytes);
    }

    @Override
    public void deleteProcessModel(Long id) {
        ProcessModelTable processModel = processModelDao.find(id);
        ProjectTable project = processModel.getProject();

        List<Long> relatedProcessModelIdsToDelete = getRelatedProcessModelsToDelete(id, new ArrayList<>());
        relatedProcessModelIdsToDelete.forEach(processModelId -> {
            dataStoreConnectionDao.deleteForProcessModel(processModelId);
            processConnectionDao.deleteForProcessModel(processModelId);
			messageFlowDao.deleteForProcessModel(processModelId);
            relatedProcessModelDao.deleteByRelatedProcessModelId(processModelId);
        });

        processModelDao.delete(relatedProcessModelIdsToDelete);
        relatedProcessModelRepository.calculateAndSaveRelatedProcessModels(project);
    }

    private List<Long> getRelatedProcessModelsToDelete(Long id, List<Long> processModelIdsToDelete) {
        if (processModelIdsToDelete.contains(id)) {
            return processModelIdsToDelete;
        }

        processModelIdsToDelete.add(id);
        ProcessModelTable table = processModelDao.findWithParentsAndChildren(id);

        List<ProcessModelTable> children = table.getChildren();
        for (ProcessModelTable child : children) {
            boolean hasOtherParent = child.getParents().size() > 1;
            if (!hasOtherParent) {
                processModelIdsToDelete = getRelatedProcessModelsToDelete(child.getId(), processModelIdsToDelete);
            }
        }

        List<ProcessModelTable> parents = table.getParents();
        for (ProcessModelTable parent : parents) {
            processModelIdsToDelete = getRelatedProcessModelsToDelete(parent.getId(), processModelIdsToDelete);
        }

        return processModelIdsToDelete;
    }

    @Override
    public ProcessModelTable findByNameOrBpmnProcessId(String name, String bpmnProcessId, Long projectId) {
        ProjectTable project = new ProjectTable();
        project.setId(projectId);
        ProcessModelTable processModel = processModelDao.findByName(name, project);
        if (processModel != null) {
            return processModel;
        }
        return processModelDao.findByBpmnProcessId(bpmnProcessId, project);
    }
}
