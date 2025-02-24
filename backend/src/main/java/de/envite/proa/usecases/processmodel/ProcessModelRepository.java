package de.envite.proa.usecases.processmodel;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.process.ProcessDetails;
import de.envite.proa.entities.process.ProcessInformation;
import de.envite.proa.entities.process.ProcessModel;
import de.envite.proa.repository.tables.ProcessModelTable;

import java.util.List;

public interface ProcessModelRepository {

	Long saveProcessModel(Long projectId, ProcessModel processModel);

	String getProcessModelXml(Long id);

	List<ProcessInformation> getProcessInformation(Long projectId);

	ProcessDetails getProcessDetails(Long id);

	void deleteProcessModel(Long id);

	void saveMessageFlows(List<MessageFlowDetails> messageFlows, Long projectId);

	ProcessModelTable findByNameOrBpmnProcessId(String name, String bpmnProcessId, Long projectId);

	ProcessModelTable getProcessModel(Long id);
}