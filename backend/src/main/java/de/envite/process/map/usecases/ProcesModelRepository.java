package de.envite.process.map.usecases;

import java.util.List;

import de.envite.process.map.entities.ProcessInformation;
import de.envite.process.map.entities.ProcessModel;

public interface ProcesModelRepository {

	Long saveProcessModel(ProcessModel processModel);

	String getProcessModel(Long id);

	List<ProcessInformation> getProcessModels();
}