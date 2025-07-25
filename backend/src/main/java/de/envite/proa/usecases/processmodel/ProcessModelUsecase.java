package de.envite.proa.usecases.processmodel;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.collaboration.ParticipantDetails;
import de.envite.proa.entities.process.*;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.usecases.ProcessOperations;
import de.envite.proa.usecases.processmap.ProcessMapRespository;
import de.envite.proa.usecases.processmodel.exceptions.CantReplaceWithCollaborationException;
import de.envite.proa.usecases.processmodel.exceptions.CollaborationAlreadyExistsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

@ApplicationScoped
public class ProcessModelUsecase {

	@Inject
	private ProcessModelRepository repository;

	@Inject
	private ProcessOperations processOperations;

	@Inject
	private ProcessMapRespository processMapRepository;

	public Long saveProcessModel(Long projectId, String name, String xml, String description,
			boolean isUploadedProcessCollaboration)
			throws CantReplaceWithCollaborationException, CollaborationAlreadyExistsException {
		String bpmnProcessId = processOperations.getBpmnProcessId(xml);
		List<ProcessModelTable> existingProcessModels = repository.getByNameOrBpmnProcessId(name,
				bpmnProcessId, projectId);

		if (!existingProcessModels.isEmpty()) {
			ProcessModelTable existingNonCollaborationProcess = null;
			ProcessModelTable existingCollaborationProcess = null;

			for (ProcessModelTable model : existingProcessModels) {
				if (model.getProcessType() == ProcessType.COLLABORATION) {
					existingCollaborationProcess = model;
				} else {
					existingNonCollaborationProcess = model;
				}
			}

			if (existingCollaborationProcess != null && isUploadedProcessCollaboration) {
				throw new CollaborationAlreadyExistsException(bpmnProcessId, name);
			}
			if (existingNonCollaborationProcess != null && !isUploadedProcessCollaboration) {
				return replaceProcessModel(projectId, existingNonCollaborationProcess.getId(), name, xml, description);
			}
		}

		if (isUploadedProcessCollaboration) {
			xml = processOperations.addEmptyProcessRefs(xml);
		}

		ProcessModel processModel = createProcessModel(name, description, xml, isUploadedProcessCollaboration);

		if (!isUploadedProcessCollaboration) {
			return repository.saveProcessModel(projectId, processModel);
		}

		List<ParticipantDetails> participants = processOperations.getParticipants(xml);
		Long collaborationId = repository.saveProcessModel(projectId, processModel);

		Map<String, Long> bpmnIdToIdMap = new HashMap<>();

		participants //
				.forEach(participant -> { //
					String participantBpmnProcessId = processOperations.getBpmnProcessId(participant.getXml());
					ProcessModelTable duplicateProcessModel = repository.findByNameOrBpmnProcessIdWithoutCollaborations(
							participant.getName(), participantBpmnProcessId, projectId);
					Long participantId = saveParticipant(projectId, participant.getName(), participant.getXml(),
							participant.getDescription(), processModel.getBpmnProcessId());
					if (duplicateProcessModel != null) {
						bpmnIdToIdMap.forEach((key, value) -> {
							if (value.equals(duplicateProcessModel.getId())) {
								bpmnIdToIdMap.replace(key, participantId);
							}
						});
					}
					bpmnIdToIdMap.put(participantBpmnProcessId, participantId);
				});

		List<MessageFlowDetails> messageFlows = processOperations.getMessageFlows(xml, bpmnIdToIdMap);
		repository.saveMessageFlows(messageFlows, projectId);

		return collaborationId;
	}

	private Long saveParticipant(Long projectId, String name, String xml, String description,
			String parentBpmnProcessId) {
		String bpmnProcessId = processOperations.getBpmnProcessId(xml);
		ProcessModelTable existingProcessModel = repository.findByNameOrBpmnProcessIdWithoutCollaborations(name,
				bpmnProcessId, projectId);
		if (existingProcessModel != null) {
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
		Set<ProcessEvent> startEvents = processOperations.getStartEvents(xml);
		Set<ProcessEvent> intermediateThrowEvents = processOperations.getIntermediateThrowEvents(xml);
		Set<ProcessEvent> intermediateCatchEvents = processOperations.getIntermediateCatchEvents(xml);
		Set<ProcessEvent> endEvents = processOperations.getEndEvents(xml);
		Set<ProcessEvent> events = new HashSet<>(startEvents);
		events.addAll(intermediateThrowEvents);
		events.addAll(intermediateCatchEvents);
		events.addAll(endEvents);
		Set<ProcessActivity> callActivities = processOperations.getCallActivities(xml);
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
			String description) throws CantReplaceWithCollaborationException {
		boolean isCollaboration = processOperations.getIsCollaboration(content);
		if (isCollaboration) {
			throw new CantReplaceWithCollaborationException(oldProcessId);
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