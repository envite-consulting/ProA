package de.envite.proa.usecases;

import de.envite.proa.entities.process.RelatedProcessModel;
import de.envite.proa.repository.tables.RelatedProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;

import java.util.List;

public interface RelatedProcessModelRepository {
    void addRelatedProcessModel(Long projectId, Long id, List<Long> relatedProcessModelIds);

    void calculateAndSaveRelatedProcessModels(ProjectTable projectTable);

    RelatedProcessModel mapToRelatedProcessModel(RelatedProcessModelTable relatedProcessModelTable);
}
