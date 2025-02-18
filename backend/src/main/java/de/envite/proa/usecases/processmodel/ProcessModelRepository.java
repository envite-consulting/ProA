package de.envite.proa.usecases.processmodel;

import java.util.List;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.process.ProcessDetails;
import de.envite.proa.entities.process.ProcessInformation;
import de.envite.proa.entities.process.ProcessModel;
import de.envite.proa.repository.tables.ProcessModelTable;

public interface ProcessModelRepository {

	Long saveProcessModel(Long projectId, ProcessModel processModel);

	String getProcessModelXml(Long id);

    void addRelatedProcessModel(Long projectId, Long id, List<Long> relatedProcessModelIds);

	List<ProcessInformation> getProcessInformation(Long projectId, String levelParam);

	ProcessInformation getProcessInformationById(Long projectId, Long id);

	ProcessDetails getProcessDetails(Long id, boolean aggregate);

	void deleteProcessModel(Long id);

	void saveMessageFlows(List<MessageFlowDetails> messageFlows, Long projectId);

	ProcessModelTable findByNameOrBpmnProcessId(String name, String bpmnProcessId, Long projectId);

	ProcessModelTable getProcessModel(Long id);
}
