package de.envite.proa.usecases.processmodel;

import de.envite.proa.entities.process.RelatedProcessModel;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.RelatedProcessModelTable;

import java.util.List;

public interface RelatedProcessModelRepository {
	void addRelatedProcessModel(Long projectId, Long id, List<Long> relatedProcessModelIds);

	void calculateAndSaveRelatedProcessModels(ProjectTable projectTable);

	List<Long> getRelatedProcessModelIdsByProcessId(Long processId);

	RelatedProcessModel mapToRelatedProcessModel(RelatedProcessModelTable relatedProcessModelTable);
}