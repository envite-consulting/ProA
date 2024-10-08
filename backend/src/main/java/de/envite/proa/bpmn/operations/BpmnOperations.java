package de.envite.proa.bpmn.operations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.envite.proa.entities.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.SourceRef;
import org.camunda.bpm.model.bpmn.impl.instance.TargetRef;
import org.camunda.bpm.model.bpmn.instance.*;

import de.envite.proa.usecases.ProcessOperations;
import jakarta.enterprise.context.ApplicationScoped;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

@ApplicationScoped
public class BpmnOperations implements ProcessOperations {

	private enum AnchorType {
		SOURCE, TARGET
	}

	@AllArgsConstructor
	@NoArgsConstructor
	private static class MessageFlowNode {
		private String id;
		private ProcessElementType type;
	}

	@AllArgsConstructor
	@NoArgsConstructor
	private static class MessageFlowEdge {
		private MessageFlowNode source;
		private MessageFlowNode target;
	}

	@Override
	public List<ProcessEvent> getStartEvents(String processModel) {
		return getEvents(processModel, EventType.START, StartEvent.class);
	}

	@Override
	public List<ProcessEvent> getIntermediateThrowEvents(String processModel) {
		return getEvents(processModel, EventType.INTERMEDIATE_THROW, IntermediateThrowEvent.class);
	}

	@Override
	public List<ProcessEvent> getIntermediateCatchEvents(String processModel) {
		return getEvents(processModel, EventType.INTERMEDIATE_CATCH, IntermediateCatchEvent.class);
	}

	@Override
	public List<ProcessEvent> getEndEvents(String processModel) {
		return getEvents(processModel, EventType.END, EndEvent.class);
	}

	@Override
	public List<ProcessActivity> getCallActivities(String processModel) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		Collection<CallActivity> callActivities = processModelInstance.getModelElementsByType(CallActivity.class);

		return callActivities//
				.stream()//
				.map(activity -> new ProcessActivity(activity.getId(), activity.getName()))//
				.collect(Collectors.toList());
	}

	@Override
	public String getDescription(String processModel) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		Collection<Collaboration> collaborations = processModelInstance.getModelElementsByType(Collaboration.class);
		if (!collaborations.isEmpty()) {
			Collaboration collaboration = collaborations.iterator().next();
			Collection<Documentation> collaborationDocumentations = collaboration.getDocumentations();
			if (!collaborationDocumentations.isEmpty()) {
				return collaborationDocumentations.iterator().next().getTextContent();
			}
		}

		Process process = processModelInstance.getModelElementsByType(Process.class).iterator().next();

		Collection<Documentation> documentations = process.getDocumentations();
		if (documentations.isEmpty()) {
			return null;
		}
		Documentation documentation = documentations.iterator().next();

		return documentation != null ? documentation.getTextContent() : null;
	}

	@Override
	public String getBpmnProcessId(String processModel) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);
		Collection<Collaboration> collaborations = processModelInstance.getModelElementsByType(Collaboration.class);
		Collection<Process> processes = processModelInstance.getModelElementsByType(Process.class);
		Collection<Participant> participants = processModelInstance.getModelElementsByType(Participant.class);

		if (!collaborations.isEmpty() && participants.size() > 1) {
			Collaboration collaboration = collaborations.iterator().next();

			if (collaboration.getId() != null && !collaboration.getId().isEmpty()) {
				return collaboration.getId();
			}

			return "Collaboration-" + participants.stream().map(p -> p.getAttributeValue("processRef"))
					.collect(Collectors.joining("-"));
		}

		if (participants.size() == 1) {
			return participants.iterator().next().getAttributeValue("processRef");
		}

		return processes.iterator().next().getId();
	}

	@Override
	public List<ParticipantDetails> getParticipants(String xml) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(xml);
		Collection<Participant> participants = processModelInstance.getModelElementsByType(Participant.class);

		String updatedXml = Bpmn.convertToString(processModelInstance);
		return participants //
				.stream() //
				.map(participant -> new ParticipantDetails( //
						participant.getName() != null ? participant.getName() : participant.getId(), //
						getParticipantDescription(participant), //
						extractParticipantXml(updatedXml, participant.getAttributeValue("processRef")) //
				)) //
				.collect(Collectors.toList());
	}

	@Override
	public List<MessageFlowDetails> getMessageFlows(String xml, Map<String, Long> bpmnIdToIdMap) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(xml);
		Collection<MessageFlow> messageFlows = processModelInstance.getModelElementsByType(MessageFlow.class);
		if (messageFlows.isEmpty()) {
			return null;
		}
		List<MessageFlowDetails> messageFlowDetails = new ArrayList<>();
		for (MessageFlow messageFlow : messageFlows) {
			MessageFlowDetails messageFlowDetail = new MessageFlowDetails();
			messageFlowDetail.setBpmnId(messageFlow.getId());
			messageFlowDetail.setName(messageFlow.getName());
			messageFlowDetail.setDescription(messageFlow.getDocumentations().isEmpty()
					? null
					: messageFlow.getDocumentations().iterator().next().getTextContent());
			MessageFlowEdge messageFlowEdge = getMessageFlowEdge(messageFlow);
			Long callingProcessId = bpmnIdToIdMap.get(messageFlowEdge.source.id);
			Long calledProcessId = bpmnIdToIdMap.get(messageFlowEdge.target.id);
			messageFlowDetail.setCallingProcessId(callingProcessId);
			messageFlowDetail.setCalledProcessId(calledProcessId);
			messageFlowDetail.setCallingElementType(messageFlowEdge.source.type);
			messageFlowDetail.setCalledElementType(messageFlowEdge.target.type);
			messageFlowDetails.add(messageFlowDetail);
		}
		return messageFlowDetails;
	}

	@Override
	public String addEmptyProcessRefs(String xml) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(xml);
		Collection<Participant> participants = processModelInstance.getModelElementsByType(Participant.class);
		for (Participant participant : participants) {
			if (participant.getProcess() == null) {
				participant.setAttributeValue("processRef", "Process_" + participant.getId());
			}
		}
		return Bpmn.convertToString(processModelInstance);
	}

	private MessageFlowEdge getMessageFlowEdge(MessageFlow messageFlow) {
		return new MessageFlowEdge( //
				getMessageFlowNode(messageFlow.getSource(), AnchorType.SOURCE), //
				getMessageFlowNode(messageFlow.getTarget(), AnchorType.TARGET) //
		);
	}

	private MessageFlowNode getMessageFlowNode(ModelElementInstance element, AnchorType anchorType) {
		if (anchorType == AnchorType.TARGET && element instanceof Participant) {
			return new MessageFlowNode(element.getAttributeValue("processRef"), ProcessElementType.START_EVENT);
		} else if (anchorType == AnchorType.SOURCE && element instanceof Participant) {
			return new MessageFlowNode(element.getAttributeValue("processRef"), ProcessElementType.CALL_ACTIVITY);
		} else if (anchorType == AnchorType.TARGET && element instanceof Activity) {
			return new MessageFlowNode(getProcessIdFromChildElement(element),
					ProcessElementType.INTERMEDIATE_CATCH_EVENT);
		} else if (element instanceof Activity) {
			return new MessageFlowNode(getProcessIdFromChildElement(element), ProcessElementType.CALL_ACTIVITY);
		} else if (element instanceof EndEvent) {
			return new MessageFlowNode(getProcessIdFromChildElement(element), ProcessElementType.END_EVENT);
		} else if (element instanceof IntermediateCatchEvent) {
			return new MessageFlowNode(getProcessIdFromChildElement(element),
					ProcessElementType.INTERMEDIATE_CATCH_EVENT);
		} else if (element instanceof IntermediateThrowEvent) {
			return new MessageFlowNode(getProcessIdFromChildElement(element),
					ProcessElementType.INTERMEDIATE_THROW_EVENT);
		} else if (element instanceof StartEvent) {
			return new MessageFlowNode(getProcessIdFromChildElement(element), ProcessElementType.START_EVENT);
		}
		return null;
	}

	private String getProcessIdFromChildElement(ModelElementInstance element) {
		ModelElementInstance process = element.getParentElement();
		while (!(process instanceof Process)) {
			process = process.getParentElement();
		}
		return ((Process) process).getId();
	}

	private String getParticipantDescription(Participant participant) {
		Collection<Documentation> participantDocumentations = participant.getDocumentations();
		if (!participantDocumentations.isEmpty()) {
			return participantDocumentations.iterator().next().getTextContent();
		}

		if (participant.getProcess() == null) {
			return null;
		}

		Collection<Documentation> processDocumentations = participant.getProcess().getDocumentations();
		if (!processDocumentations.isEmpty()) {
			return processDocumentations.iterator().next().getTextContent();
		}

		return null;
	}

	private String extractParticipantXml(String collaborationXml, String participantProcessRef) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(collaborationXml);

		Collection<Participant> participants = processModelInstance.getModelElementsByType(Participant.class);
		participants.forEach(participant -> {
			if (!participant.getAttributeValue("processRef").equals(participantProcessRef)) {
				participant.getParentElement().removeChildElement(participant);
			}
		});

		Collection<Process> processes = processModelInstance.getModelElementsByType(Process.class);
		processes.forEach(process -> {
			if (!process.getId().equals(participantProcessRef)) {
				process.getParentElement().removeChildElement(process);
			}
		});

		Collection<MessageFlow> messageFlows = processModelInstance.getModelElementsByType(MessageFlow.class);
		messageFlows.forEach(messageFlow -> messageFlow.getParentElement().removeChildElement(messageFlow));

		return Bpmn.convertToString(processModelInstance);
	}

	private <T extends Event> List<ProcessEvent> getEvents(String processModel, EventType eventType,
			Class<T> eventClass) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		Collection<T> startEvents = processModelInstance.getModelElementsByType(eventClass);
		return startEvents//
				.stream()//
				.map(event -> new ProcessEvent(event.getId(), event.getName(), eventType))//
				.collect(Collectors.toList());
	}

	private BpmnModelInstance getProcessModelInstance(String processModel) {
		InputStream processModelStream = new ByteArrayInputStream(processModel.getBytes(StandardCharsets.UTF_8));
		return Bpmn.readModelFromStream(processModelStream);
	}

	@Override
	public List<ProcessDataStore> getDataStores(String processModel) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		Collection<DataStoreReference> dataStores = processModelInstance
				.getModelElementsByType(DataStoreReference.class);

		Collection<DataAssociation> dataAssociations = processModelInstance
				.getModelElementsByType(DataAssociation.class);

		return dataStores//
				.stream()//
				.map(store -> {

					DataAccess access = getDataStoreAccess(dataAssociations, store);
					return new ProcessDataStore(store.getId(), store.getName(), access);
				})//
				.collect(Collectors.toList());
	}

	private DataAccess getDataStoreAccess(Collection<DataAssociation> dataAssociations, DataStoreReference store) {
		boolean isStoreTarget = dataAssociations//
				.stream()//
				.flatMap(association -> association.getChildElementsByType(TargetRef.class).stream())//
				.anyMatch(target -> target.getRawTextContent().equals(store.getId()));

		boolean isStoreSource = dataAssociations//
				.stream()//
				.flatMap(association -> association.getChildElementsByType(SourceRef.class).stream())//
				.anyMatch(target -> target.getRawTextContent().equals(store.getId()));

		DataAccess access;
		if (isStoreSource && isStoreTarget) {
			access = DataAccess.READ_WRITE;
		} else if (isStoreSource) {
			access = DataAccess.READ;
		} else if (isStoreTarget) {
			access = DataAccess.WRITE;
		} else {
			access = DataAccess.NONE;
		}
		return access;
	}

	@Override
	public boolean getIsCollaboration(String xml) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(xml);
		return processModelInstance.getModelElementsByType(Participant.class).size() > 1;
	}
}