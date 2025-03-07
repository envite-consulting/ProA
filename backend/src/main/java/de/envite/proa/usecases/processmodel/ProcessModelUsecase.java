package de.envite.proa.usecases.processmodel;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.collaboration.ParticipantDetails;
import de.envite.proa.entities.process.*;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.usecases.ProcessOperations;
import de.envite.proa.usecases.processmap.ProcessMapRespository;
import de.envite.proa.usecases.processmodel.exceptions.CantReplaceWithCollaborationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;

@ApplicationScoped
public class ProcessModelUsecase {

	private final ProcessModelRepository repository;
	private final ProcessOperations processOperations;
	private final ProcessMapRespository processMapRepository;

	@Inject
	public ProcessModelUsecase(ProcessModelRepository repository, //
							   ProcessOperations processOperations, //
							   ProcessMapRespository processMapRepository) {
		this.repository = repository;
		this.processOperations = processOperations;
		this.processMapRepository = processMapRepository;
	}

	public Long saveProcessModel(Long projectId, String name, String xml, String description,
								 boolean isUploadedProcessCollaboration) throws CantReplaceWithCollaborationException {
		String bpmnProcessId = processOperations.getBpmnProcessId(xml);
		ProcessModelTable existingProcessModel = repository.findByNameOrBpmnProcessIdWithoutCollaborations(name,
				bpmnProcessId, projectId);

		if (existingProcessModel != null && !isUploadedProcessCollaboration) {
			return replaceProcessModel(projectId, existingProcessModel.getId(), name, xml, description, false, null);
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

	public void addRelatedProcessModel(Long projectId, Long id, List<Long> relatedProcessModelIds) {
		repository.addRelatedProcessModel(projectId, id, relatedProcessModelIds);
	}

	public String getProcessModel(Long id) {
		return repository.getProcessModelXml(id);
	}

	public List<ProcessInformation> getProcessInformation(Long projectId, String levelParam) {
		return repository.getProcessInformation(projectId, levelParam);
	}

	public ProcessInformation getProcessInformationById(Long projectId, Long id) {
		return repository.getProcessInformationById(projectId, id);
	}

	public ProcessDetails getProcessDetails(Long id, boolean aggregate) {
		return repository.getProcessDetails(id, aggregate);
	}

	public void deleteProcessModel(Long id) {
		repository.deleteProcessModel(id);
	}

	public Long replaceProcessModel(Long projectId, Long oldProcessId, String fileName, String content,
			String description, boolean startProcessChangeAnalysis, String geminiApiKey)
			throws CantReplaceWithCollaborationException {
		boolean isCollaboration = processOperations.getIsCollaboration(content);

		if (isCollaboration) {
			throw new CantReplaceWithCollaborationException(oldProcessId);
		}

		if (startProcessChangeAnalysis) {
			repository.handleProcessChangeAnalysis(oldProcessId, content, geminiApiKey);
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
