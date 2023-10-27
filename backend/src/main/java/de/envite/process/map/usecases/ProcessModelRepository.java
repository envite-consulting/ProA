package de.envite.process.map.usecases;

import java.util.List;

import de.envite.process.map.entities.ProcessDetails;
import de.envite.process.map.entities.ProcessInformation;
import de.envite.process.map.entities.ProcessModel;

public interface ProcessModelRepository {

	Long saveProcessModel(ProcessModel processModel);

	String getProcessModel(Long id);

	List<ProcessInformation> getProcessInformation();

	ProcessDetails getProcessDetails(Long id);
}