package de.envite.proa.usecases;

import java.util.List;

import de.envite.proa.entities.*;
import de.envite.proa.repository.tables.ProcessModelTable;

public interface ProcessModelRepository {

	Long saveProcessModel(Long projectId, ProcessModel processModel);

	void addRelatedProcessModel(Long projectId, Long id, List<Long> relatedProcessModelIds);

	String getProcessModel(Long id);

	List<ProcessInformation> getProcessInformation(Long projectId, String levelParam);

	ProcessInformation getProcessInformationById(Long projectId, Long id);

	ProcessDetails getProcessDetails(Long id, boolean aggregate);

	void deleteProcessModel(Long id);

	void saveMessageFlows(List<MessageFlowDetails> messageFlows, Long projectId);

	ProcessModelTable findByNameOrBpmnProcessId(String name, String bpmnProcessId, Long projectId);
}
