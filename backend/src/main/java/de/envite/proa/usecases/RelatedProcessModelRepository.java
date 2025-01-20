package de.envite.proa.usecases;

import de.envite.proa.entities.RelatedProcessModel;
import de.envite.proa.repository.tables.RelatedProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;

public interface RelatedProcessModelRepository {
    void calculateAndSaveRelatedProcessModels(ProjectTable projectTable);

    RelatedProcessModel mapToRelatedProcessModel(RelatedProcessModelTable relatedProcessModelTable);
}
