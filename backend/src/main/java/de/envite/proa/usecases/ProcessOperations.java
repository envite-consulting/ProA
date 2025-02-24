package de.envite.proa.usecases;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.collaboration.ParticipantDetails;
import de.envite.proa.entities.process.ProcessActivity;
import de.envite.proa.entities.process.ProcessDataStore;
import de.envite.proa.entities.process.ProcessEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProcessOperations {

	Set<ProcessEvent> getStartEvents(String processModel);

	Set<ProcessEvent> getEndEvents(String processModel);

	Set<ProcessActivity> getCallActivities(String xml);

	Set<ProcessEvent> getIntermediateThrowEvents(String xml);

	Set<ProcessEvent> getIntermediateCatchEvents(String xml);

	Set<ProcessDataStore> getDataStores(String xml);

	String getDescription(String xml);

	String getBpmnProcessId(String xml);

	List<ParticipantDetails> getParticipants(String xml);

	List<MessageFlowDetails> getMessageFlows(String xml, Map<String, Long> bpmnIdToIdMap);

	String addEmptyProcessRefs(String xml);

	boolean getIsCollaboration(String xml);
}