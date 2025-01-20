package de.envite.proa.repository;

import de.envite.proa.entities.EventType;
import de.envite.proa.entities.RelatedProcessModel;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.RelatedProcessModelTable;
import de.envite.proa.usecases.RelatedProcessModelRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class RelatedProcessModelRepositoryImpl implements RelatedProcessModelRepository {

    private final ProcessModelDao processModelDao;
    private final RelatedProcessModelDao relatedProcessModelDao;

    @Inject
    public RelatedProcessModelRepositoryImpl(ProcessModelDao processModelDao, RelatedProcessModelDao relatedProcessModelDao) {
        this.processModelDao = processModelDao;
        this.relatedProcessModelDao = relatedProcessModelDao;
    }

    public void calculateAndSaveRelatedProcessModels(ProjectTable projectTable) {
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
            List<RelatedProcessModelTable> relatedProcessModels = new ArrayList<>();

            for (int i = 0; i < matchingModels.size(); i++) {
                ProcessModelTable otherModel = matchingModels.get(i);
                int level = rootLevel + i;

                if (otherModel.equals(model)) {
                    model.setLevel(level);
                    processModelDao.merge(model);
                } else {
                    RelatedProcessModelTable relatedProcessModel = new RelatedProcessModelTable();
                    relatedProcessModel.setProcessModel(model);
                    relatedProcessModel.setRelatedProcessModelId(otherModel.getId());
                    relatedProcessModel.setProcessName(otherModel.getName());
                    relatedProcessModel.setLevel(level);
                    relatedProcessModels.add(relatedProcessModel);
                }
            }

            relatedProcessModelDao.deleteByProcessModel(model);
            relatedProcessModels.forEach(relatedProcessModelDao::merge);
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
    public RelatedProcessModel mapToRelatedProcessModel(RelatedProcessModelTable relatedProcessModelTable) {
        RelatedProcessModel relatedProcessModel = new RelatedProcessModel();
        relatedProcessModel.setRelatedProcessModelId(relatedProcessModelTable.getRelatedProcessModelId());
        relatedProcessModel.setProcessName(relatedProcessModelTable.getProcessName());
        relatedProcessModel.setLevel(relatedProcessModelTable.getLevel());

        return relatedProcessModel;
    }
}
