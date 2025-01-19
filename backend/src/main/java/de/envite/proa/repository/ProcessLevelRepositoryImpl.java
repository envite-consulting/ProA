package de.envite.proa.repository;

import de.envite.proa.entities.EventType;
import de.envite.proa.entities.ProcessLevel;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessLevelTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.usecases.ProcessLevelRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class ProcessLevelRepositoryImpl implements ProcessLevelRepository {

    private final ProcessLevelDao processLevelDao;
    private final ProcessModelDao processModelDao;

    @Inject
    public ProcessLevelRepositoryImpl(ProcessLevelDao processLevelDao, ProcessModelDao processModelDao) {
        this.processLevelDao = processLevelDao;
        this.processModelDao = processModelDao;
    }

    public void calculateAndSaveProcessLevels(ProjectTable projectTable) {
        List<ProcessModelTable> allModels = processModelDao.getProcessModels(projectTable, null)
                .stream()
                .filter(model -> model.getParents() == null || model.getParents().isEmpty())
                .toList();

        for (ProcessModelTable model : allModels) {
            List<String> startEventName = getStartEventNameFromBpmn(model);
            List<String> endEventName = getEndEventNameFromBpmn(model);

            // TODO: Start or event name is null or empty
            if (startEventName == null || startEventName.isEmpty() ||
            endEventName == null || endEventName.isEmpty()) {
                return;
            }

            List<ProcessModelTable> matchingModels = allModels.stream()
                    .filter(otherModel -> {
                        List<String> otherStartEventNames = getStartEventNameFromBpmn(otherModel);
                        List<String> otherEndEventNames = getEndEventNameFromBpmn(otherModel);

                        return startEventName.stream().anyMatch(otherStartEventNames::contains) &&
                               endEventName.stream().anyMatch(otherEndEventNames::contains);
                    })
                    .sorted(Comparator.comparingInt(otherModel -> otherModel.getBpmnXml().length()))
                    .toList();

            if (matchingModels.size() <= 1) {
                model.setLevel(null);
                processModelDao.merge(model);
                return;
            }

            int rootLevel = 1;
            List<ProcessLevelTable> levels = new ArrayList<>();

            for (int i = 0; i < matchingModels.size(); i++) {
                ProcessModelTable otherModel = matchingModels.get(i);
                int level = rootLevel + i;

                if (otherModel.equals(model)) {
                    model.setLevel(level);
                    processModelDao.merge(model);
                } else {
                    ProcessLevelTable processLevel = new ProcessLevelTable();
                    processLevel.setProcessModel(model);
                    processLevel.setRelatedProcessModelId(otherModel.getId());
                    processLevel.setProcessName(otherModel.getName());
                    processLevel.setLevel(level);
                    levels.add(processLevel);
                }
            }

            processLevelDao.deleteByProcessModel(model);
            levels.forEach(processLevelDao::merge);
        }
    }

    private List<String> getStartEventNameFromBpmn(ProcessModelTable model) {
        if (model != null && !model.getEvents().isEmpty()) {
            return model.getEvents().stream()
                    .filter(event -> event.getEventType() == EventType.START)
                    .map(ProcessEventTable::getLabel)
                    .filter(label -> label != null && !label.isEmpty())
                    .toList();
        }
        return Collections.emptyList();
    }

    private List<String> getEndEventNameFromBpmn(ProcessModelTable model) {
        if (model != null && !model.getEvents().isEmpty()) {
            return model.getEvents().stream()
                    .filter(event -> event.getEventType() == EventType.END)
                    .map(ProcessEventTable::getLabel)
                    .filter(label -> label != null && !label.isEmpty())
                    .toList();
        }
        return Collections.emptyList();
    }

    @Override
    public ProcessLevel mapToProcessLevel(ProcessLevelTable levelTable) {
        ProcessLevel level = new ProcessLevel();
        level.setRelatedProcessModelId(levelTable.getRelatedProcessModelId());
        level.setProcessName(levelTable.getProcessName());
        level.setLevel(levelTable.getLevel());

        return level;
    }
}
