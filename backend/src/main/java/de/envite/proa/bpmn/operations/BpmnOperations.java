package de.envite.proa.bpmn.operations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.SourceRef;
import org.camunda.bpm.model.bpmn.impl.instance.TargetRef;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.Collaboration;
import org.camunda.bpm.model.bpmn.instance.DataAssociation;
import org.camunda.bpm.model.bpmn.instance.DataStoreReference;
import org.camunda.bpm.model.bpmn.instance.Documentation;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Participant;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.collaboration.ParticipantDetails;
import de.envite.proa.entities.datastore.DataAccess;
import de.envite.proa.entities.process.EventType;
import de.envite.proa.entities.process.ProcessActivity;
import de.envite.proa.entities.process.ProcessDataStore;
import de.envite.proa.entities.process.ProcessElementType;
import de.envite.proa.entities.process.ProcessEvent;
import de.envite.proa.usecases.ProcessOperations;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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
	public Set<ProcessEvent> getStartEvents(String processModel) {
		return getEvents(processModel, EventType.START, StartEvent.class);
	}

	@Override
	public Set<ProcessEvent> getIntermediateThrowEvents(String processModel) {
		return getEvents(processModel, EventType.INTERMEDIATE_THROW, IntermediateThrowEvent.class);
	}

	@Override
	public Set<ProcessEvent> getIntermediateCatchEvents(String processModel) {
		return getEvents(processModel, EventType.INTERMEDIATE_CATCH, IntermediateCatchEvent.class);
	}

	@Override
	public Set<ProcessEvent> getEndEvents(String processModel) {
		return getEvents(processModel, EventType.END, EndEvent.class);
	}

	@Override
	public Set<ProcessActivity> getCallActivities(String processModel) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		Collection<CallActivity> callActivities = processModelInstance.getModelElementsByType(CallActivity.class);

		return callActivities//
				.stream()//
				.map(activity -> new ProcessActivity(activity.getId(), activity.getName()))//
				.collect(Collectors.toSet());
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

		return documentation.getTextContent();
	}

	@Override
	public String getBpmnProcessId(String processModel) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);
		Collection<Collaboration> collaborations = processModelInstance.getModelElementsByType(Collaboration.class);
		Collection<Process> processes = processModelInstance.getModelElementsByType(Process.class);
		Collection<Participant> participants = processModelInstance.getModelElementsByType(Participant.class);

		if (!collaborations.isEmpty() && participants.size() > 1) {
			Collaboration collaboration = collaborations.iterator().next();

			if (collaboration.getId() != null) {
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
				.map(participant -> {
					return new ParticipantDetails( //
							getProcessNameOfParticipant(participant, processModelInstance), //
							getParticipantDescription(participant), //
							extractParticipantXml(updatedXml, participant.getAttributeValue("processRef")) //
					);
				}) //
				.collect(Collectors.toList());
	}

	private String getProcessNameOfParticipant(Participant participant, BpmnModelInstance processModelInstance) {
		
		String processId = participant.getAttributeValue("processRef");
		
		ModelElementInstance processElement = processModelInstance.getModelElementById(processId);
		
		if(processElement !=null && processElement instanceof Process process) {
			if(process.getName() !=null) {
				return process.getName();
			}
		}
		if(participant.getName()!=null) {
			return participant.getName();
		}
		return participant.getId();
	}

	@Override
	public List<MessageFlowDetails> getMessageFlows(String xml, Map<String, Long> bpmnIdToIdMap) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(xml);
		Collection<MessageFlow> messageFlows = processModelInstance.getModelElementsByType(MessageFlow.class);
		List<MessageFlowDetails> messageFlowDetails = new ArrayList<>();
		if (messageFlows.isEmpty()) {
			return messageFlowDetails;
		}
		for (MessageFlow messageFlow : messageFlows) {
			MessageFlowDetails messageFlowDetail = new MessageFlowDetails();
			messageFlowDetail.setBpmnId(messageFlow.getId());
			messageFlowDetail.setName(messageFlow.getName());
			messageFlowDetail.setDescription(messageFlow.getDocumentations().isEmpty() ? null
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
		} else {
			return new MessageFlowNode(getProcessIdFromChildElement(element), ProcessElementType.START_EVENT);
		}
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

	private String extractParticipantXml(String collaborationXml, String participantId) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(collaborationXml));
			Document xmlDoc = builder.parse(is);
			deleteOtherElements(xmlDoc, participantId);
			return toString(xmlDoc);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toString(Document doc) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Error converting Process Dom to String", ex);
		}
	}

	private void deleteOtherElements(Node processNode, String processId) {
		NodeList children = processNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Document.ELEMENT_NODE) {
				if (child.getNodeName().toLowerCase().contains("process")
						&& !((Element) child).getAttribute("id").equals(processId)) {
					child.getParentNode().removeChild(child);
					continue;
				} else if (child.getNodeName().toLowerCase().contains("participant")
						&& !((Element) child).getAttribute("processRef").equals(processId)) {
					child.getParentNode().removeChild(child);
					continue;
				} else if (child.getNodeName().toLowerCase().contains("messageFlow")) {
					child.getParentNode().removeChild(child);
					continue;
				} else {
					deleteOtherElements(child, processId);
				}
			}
		}
	}

	private <T extends Event> Set<ProcessEvent> getEvents(String processModel, EventType eventType,
			Class<T> eventClass) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		Collection<T> startEvents = processModelInstance.getModelElementsByType(eventClass);
		return startEvents//
				.stream()//
				.map(event -> new ProcessEvent(event.getId(), event.getName(), eventType))//
				.collect(Collectors.toSet());
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