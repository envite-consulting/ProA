package de.envite.proa.usecases.processmodel;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
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

	public Long saveProcessModel(Long projectId, String name, String xml, String description, boolean isCollaboration) {

		System.out.println("findByNameOrBpmnProcessId 1 " + Instant.now().toString());
		
		String bpmnProcessId = processOperations.getBpmnProcessId(xml);
		ProcessModelTable existingProcessModel = repository.findByNameOrBpmnProcessId(name, bpmnProcessId, projectId);

		System.out.println("findByNameOrBpmnProcessId 1 " + Instant.now().toString());
		
		if (existingProcessModel != null && isCollaboration) {
			throw new IllegalArgumentException("Collaboration already exists: " + bpmnProcessId);
		}

		if (existingProcessModel != null && existingProcessModel.getProcessType() != ProcessType.COLLABORATION) {
			System.out.println("replaceProcessModel");
			return replaceProcessModel(projectId, existingProcessModel.getId(), name, xml, description);
		}

		if (isCollaboration) {
			xml = processOperations.addEmptyProcessRefs(xml);
		}

		
		System.out.println("createProcessModel start " + Instant.now().toString());
		ProcessModel processModel = createProcessModel(name, description, xml, isCollaboration);
		System.out.println("createProcessModel end " + Instant.now().toString());
		
		if (!isCollaboration) {
			System.out.println("saveProcessModel !isCollaboration "+ Instant.now().toString());
			return repository.saveProcessModel(projectId, processModel);
		}

		System.out.println("getParticipants start " + Instant.now().toString());
		List<ParticipantDetails> participants = processOperations.getParticipants(xml);
		System.out.println("getParticipants end " + Instant.now().toString());
		
		System.out.println("saveProcessModel  " + Instant.now().toString());
		Long collaborationId = repository.saveProcessModel(projectId, processModel);
		System.out.println("saveProcessModel end  " + Instant.now().toString());
		
		Map<String, Long> bpmnIdToIdMap = new HashMap<>();

		participants //
				.forEach(participant -> { //
					String participantBpmnProcessId = processOperations.getBpmnProcessId(participant.getXml());
					
					System.out.println("findByNameOrBpmnProcessId start  " + Instant.now().toString());
					
					ProcessModelTable duplicateProcessModel = repository.findByNameOrBpmnProcessId(
							participant.getName(), participantBpmnProcessId, projectId);
					
					System.out.println("findByNameOrBpmnProcessId end " + Instant.now().toString());
					
					System.out.println("saveParticipant start " + Instant.now().toString());
					Long participantId = saveParticipant(projectId, participant.getName(), participant.getXml(),
							participant.getDescription(), processModel.getBpmnProcessId());
					System.out.println("saveParticipant end  " + Instant.now().toString());
					
					if (duplicateProcessModel != null && duplicateProcessModel.getProcessType() != ProcessType.COLLABORATION) {
						bpmnIdToIdMap.forEach((key, value) -> {
							if (value.equals(duplicateProcessModel.getId())) {
								bpmnIdToIdMap.replace(key, participantId);
							}
						});
					}
					bpmnIdToIdMap.put(participantBpmnProcessId, participantId);
				});

		List<MessageFlowDetails> messageFlows = processOperations.getMessageFlows(xml, bpmnIdToIdMap);
		
		System.out.println("saveMessageFlows start " + Instant.now().toString());
		repository.saveMessageFlows(messageFlows, projectId);
		System.out.println("saveMessageFlows end " + Instant.now().toString());
		
		return collaborationId;
	}

	private Long saveParticipant(Long projectId, String name, String xml, String description,
			String parentBpmnProcessId) {
		String bpmnProcessId = processOperations.getBpmnProcessId(xml);
		ProcessModelTable existingProcessModel = repository.findByNameOrBpmnProcessId(name, bpmnProcessId, projectId);

		if (existingProcessModel != null && existingProcessModel.getProcessType() != ProcessType.COLLABORATION) {
			return replaceParticipant(projectId, existingProcessModel.getId(), name, xml, description,
					parentBpmnProcessId);
		}

		ProcessModel processModel = createParticipant(name, description, xml, parentBpmnProcessId);

		return repository.saveProcessModel(projectId, processModel);
	}

	private ProcessModel createProcessModel(String name, String description, String xml, boolean isCollaboration) {
		String newDescription = (description == null || description.isBlank())
				? processOperations.getDescription(xml)
				: description;
		ProcessType processType = isCollaboration
				? ProcessType.COLLABORATION
				: ProcessType.PROCESS;

		ProcessModel processModel = getProcessModelFromXml(xml);
		processModel.setName(name);
		processModel.setDescription(newDescription);
		processModel.setProcessType(processType);

		return processModel;
	}

	private ProcessModel createParticipant(String name, String description, String xml, String parentBpmnProcessId) {
		ProcessModel processModel = getProcessModelFromXml(xml);
		processModel.setName(name);
		processModel.setDescription(description);
		processModel.setParentBpmnProcessId(parentBpmnProcessId);
		processModel.setProcessType(ProcessType.PARTICIPANT);

		return processModel;
	}

	private ProcessModel getProcessModelFromXml(String xml) {
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
		String bpmnProcessId = processOperations.getBpmnProcessId(xml);

		ProcessModel processModel = new ProcessModel();
		processModel.setBpmnXml(xml);
		processModel.setEvents(events);
		processModel.setCallActivities(callActivities);
		processModel.setDataStores(dataStores);
		processModel.setBpmnProcessId(bpmnProcessId);
		return processModel;
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
			String description) {
		boolean isCollaboration = processOperations.getIsCollaboration(content);
		if (isCollaboration) {
			throw new IllegalArgumentException("Can't replace with collaboration");
		}

		ProcessModel processModel = createProcessModel(fileName, description, content, false);
		return replaceCommonActions(projectId, oldProcessId, processModel);
	}

	private Long replaceParticipant(Long projectId, Long oldProcessId, String fileName, String content,
			String description, String parentBpmnProcessId) {
		ProcessModel processModel = createParticipant(fileName, description, content, parentBpmnProcessId);
		return replaceCommonActions(projectId, oldProcessId, processModel);
	}

	private Long replaceCommonActions(Long projectId, Long oldProcessId, ProcessModel processModel) {
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