package de.envite.proa.bpmn.operations;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.collaboration.ParticipantDetails;
import de.envite.proa.entities.datastore.DataAccess;
import de.envite.proa.entities.process.*;
import de.envite.proa.rest.FileService;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class BpmnOperationsTest {

	private static final String COLLABORATION_ID = "Collaboration_1";
	private static final String COLLABORATION_DOCUMENTATION = "This collaboration defines the interactions between the Alice and Bob teams during the order processing.";

	private static final String PROCESS_BPMN_ID_1 = "Process_1";
	private static final String PROCESS_BPMN_ID_2 = "Process_2";
	private static final Long PROCESS_ID_1 = 1L;
	private static final Long PROCESS_ID_2 = 2L;
	private static final String PROCESS_DOCUMENTATION = "This process handles the order processing workflow for Alice.";

	private static final String PARTICIPANT_NAME_1 = "Participant 1";
	private static final String PARTICIPANT_ID_2 = "Participant_2";
	private static final String PARTICIPANT_ID_3 = "Participant_3";
	private static final String PARTICIPANT_ID_4 = "Participant_4";
	private static final String PARTICIPANT_DOCUMENTATION_1 = "This is the first participant.";

	private static final String MESSAGE_FLOW_DOCUMENTATION_1 = "This is a message flow.";
	private static final String MESSAGE_FLOW_NAME_2 = "Message Flow 2";
	private static final String MESSAGE_FLOW_ID_3 = "MessageFlow_3";

	private static final FileService fileService = new FileService();
	public static final String COLLABORATION_WITH_TWO_PARTICIPANTS_BPMN = "collaboration-with-two-participants.bpmn";

	private static final Map<String, Long> bpmnIdToDatabseIdMap = new HashMap<>();
	public static final String PROCESS_BPMN = "process.bpmn";
	public static final String COLLABORATION_WITH_DOCUMENTATION_BPMN = "collaboration-with-documentation.bpmn";
	public static final String COLLABORATION_WITH_PROCESSES_NO_DOCUMENTATIONS_BPMN = "collaboration-with-processes-no-documentations.bpmn";
	public static final String PROCESS_WITH_PROCESS_DOCUMENTATION_BPMN = "process-with-process-documentation.bpmn";
	public static final String COLLABORATION_WITH_COLLABORATION_ID_BPMN = "collaboration-with-collaboration-id.bpmn";
	public static final String COLLABORATION_WITH_PROCESSES_NO_DOCUMENTATIONS_BPMN1 = "collaboration-with-processes-no-documentations.bpmn";
	public static final String COLLABORATION_ONE_PROCESS_WITH_ID_BPMN = "collaboration-one-process-with-id.bpmn";
	public static final String COLLABORATION_WITH_DETAILED_PARTICIPANTS_BPMN = "collaboration-with-detailed-participants.bpmn";
	public static final String COLLABORATION_WITH_DETAILED_MESSAGE_FLOWS_BPMN = "collaboration-with-detailed-message-flows.bpmn";
	public static final String COLLABORATION_PARTICIPANT_WITHOUT_PROCESSREF_BPMN = "collaboration-participant-without-processref.bpmn";

	@InjectMocks
	private BpmnOperations bpmnOperations;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	private static File loadFile(String fileName) {
		return new File(Objects.requireNonNull(
				BpmnOperationsTest.class.getClassLoader().getResource(fileName)).getFile());
	}

	private static Boolean isValidXml(String xml) {
		try {
			Bpmn.readModelFromStream(new ByteArrayInputStream(xml.getBytes()));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Test
	public void testStartEvents() throws IOException {
		// Arrange
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());

		// Act
		Set<ProcessEvent> startEvents = bpmnOperations.getStartEvents(bpmnXml);

		// Assert
		assertThat(startEvents)//
				.hasSize(2)//
				.extracting("elementId", "label", "eventType")//
				.contains(//
						tuple("StartEvent_1", "start 1", EventType.START),
						tuple("StartEvent_2", "start 2", EventType.START));
	}

	@Test
	public void testIntermediateCatchEvents() throws IOException {
		// Arrange
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());

		// Act
		Set<ProcessEvent> intermediateCatchEvents = bpmnOperations.getIntermediateCatchEvents(bpmnXml);

		// Assert
		assertThat(intermediateCatchEvents)//
				.hasSize(1)//
				.extracting("elementId", "label", "eventType")//
				.contains(tuple("Event_Catch", "catch", EventType.INTERMEDIATE_CATCH));
	}

	@Test
	public void testIntermediateThrowEvents() throws IOException {
		// Arrange
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());

		// Act
		Set<ProcessEvent> intermediateThrowEvents = bpmnOperations.getIntermediateThrowEvents(bpmnXml);

		// Assert
		assertThat(intermediateThrowEvents)//
				.hasSize(1)//
				.extracting("elementId", "label", "eventType")//
				.contains(tuple("Event_Throw", "throw", EventType.INTERMEDIATE_THROW));
	}

	@Test
	public void testEndEvents() throws IOException {
		// Arrange
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());

		// Act
		Set<ProcessEvent> endEvents = bpmnOperations.getEndEvents(bpmnXml);

		// Assert
		assertThat(endEvents)//
				.hasSize(2)//
				.extracting("elementId", "label", "eventType")//
				.contains(//
						tuple("EndEvent_1", "end 1", EventType.END), //
						tuple("EndEvent_2", "end 2", EventType.END));
	}

	@Test
	public void testCallActivites() throws IOException {
		// Arrange
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());

		// Act
		Set<ProcessActivity> intermediateThrowEvents = bpmnOperations.getCallActivities(bpmnXml);

		// Assert
		assertThat(intermediateThrowEvents)//
				.hasSize(1)//
				.extracting("elementId", "label")//
				.contains(tuple("Activity_Call", "Call Activity"));
	}

	@Test
	public void testDataStores() throws IOException {
		// Arrange
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram-datastores.bpmn").readAllBytes());

		// Act
		List<ProcessDataStore> intermediateThrowEvents = bpmnOperations.getDataStores(bpmnXml);

		// Assert
		assertThat(intermediateThrowEvents)//
				.hasSize(4)//
				.extracting("elementId", "label", "access")//
				.contains(//
						tuple("DataStoreReference_WriteStore", "WriteStore", DataAccess.WRITE), //
						tuple("DataStoreReference_ReadWriteStore", "ReadWriteStore", DataAccess.READ_WRITE), //
						tuple("DataStoreReference_ReadStore", "ReadStore", DataAccess.READ), //
						tuple("DataStoreReference_NoAccessStore", "NoAccessStore", DataAccess.NONE));
	}

	@Test
	public void testGetIsCollaboration_True() {
		File collaboration = loadFile(COLLABORATION_WITH_TWO_PARTICIPANTS_BPMN);
		String collaborationXml = fileService.readFileToString(collaboration);

		assertThat(bpmnOperations.getIsCollaboration(collaborationXml)).isTrue();
	}

	@Test
	public void testGetIsCollaboration_False() {
		File process = loadFile(PROCESS_BPMN);
		String processXml = fileService.readFileToString(process);

		assertThat(bpmnOperations.getIsCollaboration(processXml)).isFalse();
	}

	@Test
	public void testGetDescription_Collaboration_WithDocumentation() {
		File collaboration = loadFile(COLLABORATION_WITH_DOCUMENTATION_BPMN);
		String collaborationXml = fileService.readFileToString(collaboration);

		String result = bpmnOperations.getDescription(collaborationXml);

		assertThat(result).isEqualTo(COLLABORATION_DOCUMENTATION);
	}

	@Test
	public void testGetDescription_Collaboration_WithProcessesButNoDocumentations() {
		File collaboration = loadFile(COLLABORATION_WITH_PROCESSES_NO_DOCUMENTATIONS_BPMN);
		String collaborationXml = fileService.readFileToString(collaboration);

		String result = bpmnOperations.getDescription(collaborationXml);

		assertThat(result).isNull();
	}

	@Test
	public void testGetDescription_Process_WithDocumentation() {
		File process = loadFile(PROCESS_WITH_PROCESS_DOCUMENTATION_BPMN);
		String processXml = fileService.readFileToString(process);

		String result = bpmnOperations.getDescription(processXml);

		assertThat(result).isEqualTo(PROCESS_DOCUMENTATION);
	}

	@Test
	public void testGetBpmnProcessId_Collaboration_WithId() {
		File collaboration = loadFile(COLLABORATION_WITH_COLLABORATION_ID_BPMN);
		String collaborationXml = fileService.readFileToString(collaboration);

		String result = bpmnOperations.getBpmnProcessId(collaborationXml);

		assertThat(result).isEqualTo(COLLABORATION_ID);
	}

	@Test
	public void testGetBpmnProcessId_Collaboration_WithoutId() {
		File collaboration = loadFile(COLLABORATION_WITH_PROCESSES_NO_DOCUMENTATIONS_BPMN1);
		String collaborationXml = fileService.readFileToString(collaboration);

		String result = bpmnOperations.getBpmnProcessId(collaborationXml);

		assertThat(result).isEqualTo("Collaboration-" + PROCESS_BPMN_ID_1 + "-" + PROCESS_BPMN_ID_2);
	}

	@Test
	public void testGetBpmnProcessId_Collaboration_OneParticipant() {
		File collaboration = loadFile(COLLABORATION_ONE_PROCESS_WITH_ID_BPMN);
		String collaborationXml = fileService.readFileToString(collaboration);

		String result = bpmnOperations.getBpmnProcessId(collaborationXml);

		assertThat(result).isEqualTo(PROCESS_BPMN_ID_1);
	}

	@Test
	public void testGetBpmnProcessId_Process() {
		File process = loadFile(PROCESS_BPMN);
		String processXml = fileService.readFileToString(process);

		String result = bpmnOperations.getBpmnProcessId(processXml);

		assertThat(result).isEqualTo(PROCESS_BPMN_ID_1);
	}

	@Test
	public void testGetParticipants() {
		File collaboration = loadFile(COLLABORATION_WITH_DETAILED_PARTICIPANTS_BPMN);
		String collaborationXml = fileService.readFileToString(collaboration);

		List<ParticipantDetails> result = bpmnOperations.getParticipants(collaborationXml);

		assertThat(result).hasSize(4);

		assertThat(isValidXml(result.get(0).getXml())).isTrue();
		assertThat(isValidXml(result.get(1).getXml())).isTrue();
		assertThat(isValidXml(result.get(2).getXml())).isTrue();
		assertThat(isValidXml(result.get(3).getXml())).isTrue();

		assertThat(result.get(0).getName()).isEqualTo(PARTICIPANT_NAME_1);
		assertThat(result.get(1).getName()).isEqualTo(PARTICIPANT_ID_2);
		assertThat(result.get(2).getName()).isEqualTo(PARTICIPANT_ID_3);
		assertThat(result.get(3).getName()).isEqualTo(PARTICIPANT_ID_4);

		assertThat(result.get(0).getDescription()).isEqualTo(PARTICIPANT_DOCUMENTATION_1);
		assertThat(result.get(1).getDescription()).isEqualTo(PROCESS_DOCUMENTATION);
		assertThat(result.get(2).getDescription()).isNull();
		assertThat(result.get(3).getDescription()).isNull();
	}

	@Test
	public void testGetMessageFlows() {
		File collaboration = loadFile(COLLABORATION_WITH_DETAILED_MESSAGE_FLOWS_BPMN);
		String collaborationXml = fileService.readFileToString(collaboration);

		bpmnIdToDatabseIdMap.put(PROCESS_BPMN_ID_1, PROCESS_ID_1);
		bpmnIdToDatabseIdMap.put(PROCESS_BPMN_ID_2, PROCESS_ID_2);
		List<MessageFlowDetails> result = bpmnOperations.getMessageFlows(collaborationXml, bpmnIdToDatabseIdMap);

		assertThat(result).hasSize(8);

		assertThat(result.get(0).getBpmnId()).isNull();
		assertThat(result.get(1).getBpmnId()).isNull();
		assertThat(result.get(2).getBpmnId()).isEqualTo(MESSAGE_FLOW_ID_3);
		assertThat(result.get(3).getBpmnId()).isNull();
		assertThat(result.get(4).getBpmnId()).isNull();
		assertThat(result.get(5).getBpmnId()).isNull();
		assertThat(result.get(6).getBpmnId()).isNull();
		assertThat(result.get(7).getBpmnId()).isNull();

		assertThat(result.get(0).getName()).isNull();
		assertThat(result.get(1).getName()).isEqualTo(MESSAGE_FLOW_NAME_2);
		assertThat(result.get(2).getName()).isNull();
		assertThat(result.get(3).getName()).isNull();
		assertThat(result.get(4).getName()).isNull();
		assertThat(result.get(5).getName()).isNull();
		assertThat(result.get(6).getName()).isNull();
		assertThat(result.get(7).getName()).isNull();

		assertThat(result.get(0).getDescription()).isEqualTo(MESSAGE_FLOW_DOCUMENTATION_1);
		assertThat(result.get(1).getDescription()).isNull();
		assertThat(result.get(2).getDescription()).isNull();
		assertThat(result.get(3).getDescription()).isNull();
		assertThat(result.get(4).getDescription()).isNull();
		assertThat(result.get(5).getDescription()).isNull();
		assertThat(result.get(6).getDescription()).isNull();
		assertThat(result.get(7).getDescription()).isNull();

		assertThat(result.get(0).getCallingProcessId()).isEqualTo(PROCESS_ID_1);
		assertThat(result.get(1).getCallingProcessId()).isEqualTo(PROCESS_ID_2);
		assertThat(result.get(2).getCallingProcessId()).isEqualTo(PROCESS_ID_1);
		assertThat(result.get(3).getCallingProcessId()).isEqualTo(PROCESS_ID_2);
		assertThat(result.get(4).getCallingProcessId()).isEqualTo(PROCESS_ID_1);
		assertThat(result.get(5).getCallingProcessId()).isEqualTo(PROCESS_ID_2);
		assertThat(result.get(6).getCallingProcessId()).isEqualTo(PROCESS_ID_2);
		assertThat(result.get(7).getCallingProcessId()).isEqualTo(PROCESS_ID_1);

		assertThat(result.get(0).getCalledProcessId()).isEqualTo(PROCESS_ID_2);
		assertThat(result.get(1).getCalledProcessId()).isEqualTo(PROCESS_ID_1);
		assertThat(result.get(2).getCalledProcessId()).isEqualTo(PROCESS_ID_2);
		assertThat(result.get(3).getCalledProcessId()).isEqualTo(PROCESS_ID_1);
		assertThat(result.get(4).getCalledProcessId()).isEqualTo(PROCESS_ID_2);
		assertThat(result.get(5).getCalledProcessId()).isEqualTo(PROCESS_ID_1);
		assertThat(result.get(6).getCalledProcessId()).isEqualTo(PROCESS_ID_1);
		assertThat(result.get(7).getCalledProcessId()).isEqualTo(PROCESS_ID_2);

		assertThat(result.get(0).getCallingElementType()).isEqualTo(ProcessElementType.CALL_ACTIVITY);
		assertThat(result.get(1).getCallingElementType()).isEqualTo(ProcessElementType.CALL_ACTIVITY);
		assertThat(result.get(2).getCallingElementType()).isEqualTo(ProcessElementType.END_EVENT);
		assertThat(result.get(3).getCallingElementType()).isEqualTo(ProcessElementType.CALL_ACTIVITY);
		assertThat(result.get(4).getCallingElementType()).isEqualTo(ProcessElementType.INTERMEDIATE_THROW_EVENT);
		assertThat(result.get(5).getCallingElementType()).isEqualTo(ProcessElementType.CALL_ACTIVITY);
		assertThat(result.get(6).getCallingElementType()).isEqualTo(ProcessElementType.CALL_ACTIVITY);
		assertThat(result.get(7).getCallingElementType()).isEqualTo(ProcessElementType.CALL_ACTIVITY);

		assertThat(result.get(0).getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);
		assertThat(result.get(1).getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);
		assertThat(result.get(2).getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);
		assertThat(result.get(3).getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);
		assertThat(result.get(4).getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);
		assertThat(result.get(5).getCalledElementType()).isEqualTo(ProcessElementType.INTERMEDIATE_CATCH_EVENT);
		assertThat(result.get(6).getCalledElementType()).isEqualTo(ProcessElementType.INTERMEDIATE_CATCH_EVENT);
		assertThat(result.get(7).getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);

	}

	@Test
	public void testGetMessageFlowsEmpty() {
		File collaboration = loadFile(COLLABORATION_WITH_TWO_PARTICIPANTS_BPMN);
		String collaborationXml = fileService.readFileToString(collaboration);

		List<MessageFlowDetails> result = bpmnOperations.getMessageFlows(collaborationXml, new HashMap<>());
		assertThat(result).isEmpty();
	}

	@Test
	public void testAddEmptyProcessRefs() {
		File collaboration = loadFile(COLLABORATION_PARTICIPANT_WITHOUT_PROCESSREF_BPMN);
		String collaborationXml = fileService.readFileToString(collaboration);

		String result = bpmnOperations.addEmptyProcessRefs(collaborationXml);

		BpmnModelInstance model = Bpmn.readModelFromStream(
				new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8)));
		Collection<Participant> participants = model.getModelElementsByType(Participant.class);

		assertThat(participants).hasSize(2);
		assertThat(participants //
				.stream() //
				.map(p -> p.getAttributeValue("processRef")) //
				.allMatch(processRef -> processRef != null && !processRef.isEmpty())) //
				.isTrue();
	}
}
