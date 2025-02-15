package de.envite.proa.usecases.processmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.collaboration.ParticipantDetails;
import de.envite.proa.entities.process.*;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.usecases.ProcessOperations;
import de.envite.proa.usecases.processmap.ProcessMapRespository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProcessModelUsecase {

	@Inject
	private ProcessModelRepository repository;

	@Inject
	private ProcessOperations processOperations;

	@Inject
	private ProcessMapRespository processMapRepository;

	public Long saveProcessModel(Long projectId, String name, String xml, String description, String isCollaboration,
			String parentBpmnProcessId) {

		String bpmnProcessId = processOperations.getBpmnProcessId(xml);
		ProcessModelTable existingProcessModel = repository.findByNameOrBpmnProcessId(name, bpmnProcessId, projectId);

		if (existingProcessModel != null && isCollaboration.equals("true")) {
			throw new IllegalArgumentException("Collaboration already exists: " + bpmnProcessId);
		}

		if (existingProcessModel != null && existingProcessModel.getProcessType() != ProcessType.COLLABORATION) {
			return replaceProcessModel(projectId, existingProcessModel.getId(), name, xml, description,
					parentBpmnProcessId);
		}

		if (isCollaboration.equals("true")) {
			xml = processOperations.addEmptyProcessRefs(xml);
		}

		ProcessModel processModel = createProcessModel(name, description, xml, parentBpmnProcessId, isCollaboration);

		if (isCollaboration.equals("false")) {
			return repository.saveProcessModel(projectId, processModel);
		}

		List<ParticipantDetails> participants = processOperations.getParticipants(xml);
		Long collaborationId = repository.saveProcessModel(projectId, processModel);

		List<Long> participantIds = new ArrayList<>();

		participants //
				.forEach(participant -> { //
					Long participantId = saveProcessModel(projectId, participant.getName(), participant.getXml(),
							participant.getDescription(), "false", processModel.getBpmnProcessId());
					participantIds.add(participantId); //
				});

		Map<String, Long> bpmnIdToIdMap = participantIds //
				.stream() //
				.collect(Collectors.toMap(id -> repository.getProcessModel(id).getBpmnProcessId(), id -> id));
		List<MessageFlowDetails> messageFlows = processOperations.getMessageFlows(xml, bpmnIdToIdMap);
		repository.saveMessageFlows(messageFlows, projectId);

		return collaborationId;
	}

	private ProcessModel createProcessModel(String name, String description, String xml, String parentBpmnProcessId,
			String isCollaboration) {
		List<ProcessEvent> startEvents = processOperations.getStartEvents(xml);
		List<ProcessEvent> intermediateThrowEvents = processOperations.getIntermediateThrowEvents(xml);
		List<ProcessEvent> intermediateCatchEvents = processOperations.getIntermediateCatchEvents(xml);
		List<ProcessEvent> endEvents = processOperations.getEndEvents(xml);
		List<ProcessEvent> events = Stream.of(startEvents, //
				intermediateThrowEvents, //
				intermediateCatchEvents, //
				endEvents //
		).flatMap(Collection::stream).collect(Collectors.toList());
		List<ProcessActivity> callActivities = processOperations.getCallActivities(xml);
		List<ProcessDataStore> dataStores = processOperations.getDataStores(xml);
		String newDescription = (description == null || description.isBlank()) && parentBpmnProcessId == null
				? processOperations.getDescription(xml)
				: description;
		String bpmnProcessId = processOperations.getBpmnProcessId(xml);
		ProcessType processType = isCollaboration.equals("true")
				? ProcessType.COLLABORATION
				: parentBpmnProcessId != null ? ProcessType.PARTICIPANT : ProcessType.PROCESS;

		return new ProcessModel(name, //
				xml, //
				events, //
				callActivities, //
				dataStores, //
				newDescription, //
				bpmnProcessId, //
				parentBpmnProcessId, //
				processType);
	}

	public String getProcessModel(Long id) {
		return repository.getProcessModelXml(id);
	}

	public List<ProcessInformation> getProcessInformation(Long projectId) {
		return repository.getProcessInformation(projectId);
	}

	public ProcessDetails getProcessDetails(Long id) {
		return repository.getProcessDetails(id);
	}

	public void deleteProcessModel(Long id) {
		repository.deleteProcessModel(id);
	}

	public Long replaceProcessModel(Long projectId, Long oldProcessId, String fileName, String content,
			String description, String parentBpmnProcessId) {

		boolean isCollaboration = processOperations.getIsCollaboration(content);
		if (isCollaboration) {
			throw new IllegalArgumentException("Can't replace with collaboration");
		}

		ProcessModel processModel = createProcessModel(fileName, description, content, parentBpmnProcessId, "false");
		Long newProcessId = repository.saveProcessModel(projectId, processModel);
		copyConnections(projectId, oldProcessId, newProcessId);
		copyMessageFlowsAndRelations(projectId, oldProcessId, newProcessId);
		deleteProcessModel(oldProcessId);
		return newProcessId;
	}

	private void copyConnections(Long projectId, Long oldProcessId, Long newProcessId) {
		processMapRepository.copyConnections(projectId, oldProcessId, newProcessId);
	}

	private void copyMessageFlowsAndRelations(Long projectId, Long oldProcessId, Long newProcessId) {
		processMapRepository.copyMessageFlowsAndRelations(projectId, oldProcessId, newProcessId);
	}
}