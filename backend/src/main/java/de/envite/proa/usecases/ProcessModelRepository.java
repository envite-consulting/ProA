package de.envite.proa.usecases;

import java.util.List;

import de.envite.proa.entities.MessageFlowDetails;
import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.entities.ProcessModel;
import de.envite.proa.repository.tables.ProcessModelTable;

public interface ProcessModelRepository {

	Long saveProcessModel(Long projectId, ProcessModel processModel);

	String getProcessModel(Long id);

	List<ProcessInformation> getProcessInformation(Long projectId);

	ProcessDetails getProcessDetails(Long id);

	void deleteProcessModel(Long id);

	void saveMessageFlows(List<MessageFlowDetails> messageFlows, Long projectId);

	ProcessModelTable findByNameOrBpmnProcessId(String name, String bpmnProcessId, Long projectId);
}