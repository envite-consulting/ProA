package de.envite.proa.usecases;

import java.util.List;
import java.util.Map;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.collaboration.ParticipantDetails;
import de.envite.proa.entities.process.ProcessActivity;
import de.envite.proa.entities.process.ProcessDataStore;
import de.envite.proa.entities.process.ProcessEvent;

public interface ProcessOperations {

	public List<ProcessEvent> getStartEvents(String processModel);

	public List<ProcessEvent> getEndEvents(String processModel);

	public List<ProcessActivity> getCallActivities(String xml);

	public List<ProcessEvent> getIntermediateThrowEvents(String xml);

	public List<ProcessEvent> getIntermediateCatchEvents(String xml);

	public List<ProcessDataStore> getDataStores(String xml);

	public String getDescription(String xml);

	public String getBpmnProcessId(String xml);

	public List<ParticipantDetails> getParticipants(String xml);

	public List<MessageFlowDetails> getMessageFlows(String xml, Map<String, Long> bpmnIdToIdMap);

	public String addEmptyProcessRefs(String xml);

	public boolean getIsCollaboration(String xml);
}