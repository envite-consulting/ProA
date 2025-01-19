package de.envite.proa.usecases;

import de.envite.proa.entities.ProcessLevel;
import de.envite.proa.repository.tables.ProcessLevelTable;
import de.envite.proa.repository.tables.ProjectTable;

public interface ProcessLevelRepository {
    void calculateAndSaveProcessLevels(ProjectTable projectTable);

    ProcessLevel mapToProcessLevel(ProcessLevelTable levelTable);
}
