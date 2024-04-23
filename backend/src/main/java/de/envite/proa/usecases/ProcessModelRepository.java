package de.envite.proa.usecases;

import java.util.List;

import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.entities.ProcessModel;

public interface ProcessModelRepository {

	Long saveProcessModel(Long projectId, ProcessModel processModel);

	String getProcessModel(Long id);

	List<ProcessInformation> getProcessInformation(Long projectId);

	ProcessDetails getProcessDetails(Long id);

	void deleteProcessModel(Long id);
}