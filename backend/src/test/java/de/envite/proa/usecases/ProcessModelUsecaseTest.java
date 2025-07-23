package de.envite.proa.usecases;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.collaboration.ParticipantDetails;
import de.envite.proa.entities.process.*;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.usecases.processmap.ProcessMapRespository;
import de.envite.proa.usecases.processmodel.ProcessModelRepository;
import de.envite.proa.usecases.processmodel.ProcessModelUsecase;
import de.envite.proa.usecases.processmodel.RelatedProcessModelRepository;
import de.envite.proa.usecases.processmodel.exceptions.CantReplaceWithCollaborationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessModelUsecaseTest {

	private static final String TEST_PROCESS_NAME = "Test Process";
	private static final String TEST_PROCESS_XML = "<xml></xml>";
	private static final String TEST_DESCRIPTION = "Description";
	private static final String TEST_BPMN_PROCESS_ID = "bpmn123";
	private static final Long TEST_PROJECT_ID = 1L;
	private static final Long TEST_PROCESS_MODEL_ID = 1L;
	private static final Long TEST_OLD_PROCESS_ID = 2L;
	private static final Long TEST_NEW_PROCESS_ID = 3L;
	private static final String REPLACED_PROCESS_NAME = "Replaced Process";

	private static final Long PARTICIPANT_ID_1 = 2L;
	private static final Long PARTICIPANT_ID_2 = 3L;
	private static final Long PARTICIPANT_ID_3 = 4L;
	private static final String PARTICIPANT_BPMN_ID = "participantBpmnId";
	private static final String PARTICIPANT_BPMN_ID_2 = "participantBpmnId2";
	private static final String PARTICIPANT_NAME_1 = "Participant 1";
	private static final String PARTICIPANT_NAME_2 = "Participant 2";
	private static final String PARTICIPANT_XML_1 = "<xml><participant/></xml>";
	private static final String PARTICIPANT_XML_2 = "<xml><participant/>2</xml>";
	private static final String PARTICIPANT_DESCRIPTION_1 = "Participant Description 1";

	private static final Set<ProcessEvent> EMPTY_PROCESS_EVENTS = Set.of();
	private static final Set<ProcessActivity> EMPTY_CALL_ACTIVITIES = Set.of();
	private static final List<ProcessDataStore> EMPTY_DATA_STORES = List.of();
	private static final List<MessageFlowDetails> EMPTY_MESSAGE_FLOWS = List.of();

	private static final boolean IS_COLLABORATION = true;
	private static final boolean IS_NOT_COLLABORATION = false;

	@Mock
	private ProcessModelRepository repository;

	@Mock
	private ProcessOperations processOperations;

	@Mock
	private ProcessMapRespository processMapRepository;

	@Mock
	private RelatedProcessModelRepository relatedProcessModelRepository;

	@InjectMocks
	private ProcessModelUsecase processModelUsecase;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testSaveProcessModel_NewProcessModel()
			throws CantReplaceWithCollaborationException {
		when(processOperations.getBpmnProcessId(TEST_PROCESS_XML)).thenReturn(TEST_BPMN_PROCESS_ID);
		when(repository.findByNameOrBpmnProcessIdWithoutCollaborations(TEST_PROCESS_NAME, TEST_BPMN_PROCESS_ID,
				TEST_PROJECT_ID))
				.thenReturn(null);

		// CREATE PROCESS MODEL
		when(processOperations.getStartEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateThrowEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateCatchEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getEndEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getCallActivities(TEST_PROCESS_XML)).thenReturn(EMPTY_CALL_ACTIVITIES);
		when(processOperations.getDataStores(TEST_PROCESS_XML)).thenReturn(EMPTY_DATA_STORES);
		when(processOperations.getDescription(TEST_PROCESS_XML)).thenReturn(TEST_DESCRIPTION);

		ProcessModel processModel = new ProcessModel(TEST_PROCESS_NAME, TEST_PROCESS_XML, EMPTY_PROCESS_EVENTS,
				EMPTY_CALL_ACTIVITIES, EMPTY_DATA_STORES, TEST_DESCRIPTION,
				TEST_BPMN_PROCESS_ID, null, ProcessType.PROCESS);
		//

		when(repository.saveProcessModel(TEST_PROJECT_ID, processModel)).thenReturn(TEST_PROCESS_MODEL_ID);

		Long result = processModelUsecase.saveProcessModel(TEST_PROJECT_ID, TEST_PROCESS_NAME, TEST_PROCESS_XML,
				" ", IS_NOT_COLLABORATION);

		assertNotNull(result);
		assertEquals(TEST_PROCESS_MODEL_ID, result);

		verify(processOperations, times(2)).getBpmnProcessId(TEST_PROCESS_XML);
		verify(repository, times(1)).findByNameOrBpmnProcessIdWithoutCollaborations(TEST_PROCESS_NAME,
				TEST_BPMN_PROCESS_ID,
				TEST_PROJECT_ID);
		verify(processOperations, times(1)).getStartEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getIntermediateThrowEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getIntermediateCatchEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getEndEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getCallActivities(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getDataStores(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getDescription(TEST_PROCESS_XML);
		verify(repository, times(1)).saveProcessModel(TEST_PROJECT_ID, processModel);

		verifyNoMoreInteractions(processOperations);
		verifyNoMoreInteractions(repository);
	}

	@Test
	void testGetProcessModel() {
		when(repository.getProcessModelXml(TEST_PROCESS_MODEL_ID)).thenReturn(TEST_PROCESS_XML);

		String result = processModelUsecase.getProcessModel(TEST_PROCESS_MODEL_ID);

		assertEquals(TEST_PROCESS_XML, result);
		verify(repository, times(1)).getProcessModelXml(TEST_PROCESS_MODEL_ID);
	}

	@Test
	void testDeleteProcessModel() {
		doNothing().when(repository).deleteProcessModel(TEST_PROCESS_MODEL_ID);

		processModelUsecase.deleteProcessModel(TEST_PROCESS_MODEL_ID);

		verify(repository, times(1)).deleteProcessModel(TEST_PROCESS_MODEL_ID);
	}

	@Test
	void testSaveProcessModel_Replace()
			throws CantReplaceWithCollaborationException {
		when(processOperations.getBpmnProcessId(TEST_PROCESS_XML)).thenReturn(TEST_BPMN_PROCESS_ID);
		ProcessModelTable existingProcessModel = new ProcessModelTable();
		existingProcessModel.setId(TEST_OLD_PROCESS_ID);
		existingProcessModel.setProcessType(ProcessType.PROCESS);
		when(repository.findByNameOrBpmnProcessIdWithoutCollaborations(TEST_PROCESS_NAME, TEST_BPMN_PROCESS_ID,
				TEST_PROJECT_ID))
				.thenReturn(existingProcessModel);

		when(processOperations.getIsCollaboration(TEST_PROCESS_XML)).thenReturn(false);

		// CREATE PROCESS MODEL
		when(processOperations.getStartEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateThrowEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateCatchEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getEndEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getCallActivities(TEST_PROCESS_XML)).thenReturn(EMPTY_CALL_ACTIVITIES);
		when(processOperations.getDataStores(TEST_PROCESS_XML)).thenReturn(EMPTY_DATA_STORES);

		ProcessModel processModel = new ProcessModel(TEST_PROCESS_NAME, TEST_PROCESS_XML, EMPTY_PROCESS_EVENTS,
				EMPTY_CALL_ACTIVITIES, EMPTY_DATA_STORES, TEST_DESCRIPTION,
				TEST_BPMN_PROCESS_ID, null, ProcessType.PROCESS);
		//

		when(repository.saveProcessModel(TEST_PROJECT_ID, processModel)).thenReturn(TEST_NEW_PROCESS_ID);

		doNothing().when(processMapRepository)
				.copyConnections(TEST_PROJECT_ID, TEST_OLD_PROCESS_ID, TEST_NEW_PROCESS_ID);
		doNothing().when(processMapRepository)
				.copyMessageFlowsAndRelations(TEST_PROJECT_ID, TEST_OLD_PROCESS_ID, TEST_NEW_PROCESS_ID);
		doNothing().when(repository).deleteProcessModel(TEST_OLD_PROCESS_ID);

		Long processId = processModelUsecase.saveProcessModel(TEST_PROJECT_ID, TEST_PROCESS_NAME, TEST_PROCESS_XML,
				TEST_DESCRIPTION, IS_NOT_COLLABORATION);

		assertNotNull(processId);
		assertEquals(TEST_NEW_PROCESS_ID, processId);

		verify(processOperations, times(2)).getBpmnProcessId(TEST_PROCESS_XML);
		verify(repository, times(1)).findByNameOrBpmnProcessIdWithoutCollaborations(TEST_PROCESS_NAME,
				TEST_BPMN_PROCESS_ID,
				TEST_PROJECT_ID);
		verify(processOperations, times(1)).getIsCollaboration(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getStartEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getIntermediateThrowEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getIntermediateCatchEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getEndEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getCallActivities(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getDataStores(TEST_PROCESS_XML);
		verify(repository, times(1)).saveProcessModel(TEST_PROJECT_ID, processModel);
		verify(processMapRepository, times(1)).copyConnections(TEST_PROJECT_ID, TEST_OLD_PROCESS_ID,
				TEST_NEW_PROCESS_ID);
		verify(processMapRepository, times(1)).copyMessageFlowsAndRelations(TEST_PROJECT_ID, TEST_OLD_PROCESS_ID,
				TEST_NEW_PROCESS_ID);
		verify(repository, times(1)).deleteProcessModel(TEST_OLD_PROCESS_ID);

		verifyNoMoreInteractions(processOperations);
		verifyNoMoreInteractions(repository);
		verifyNoMoreInteractions(processMapRepository);
	}

	@Test
	void testReplaceProcessModel_IsCollaboration() {
		when(processOperations.getIsCollaboration(TEST_PROCESS_XML)).thenReturn(true);

		CantReplaceWithCollaborationException exception = assertThrows(CantReplaceWithCollaborationException.class,
				() -> processModelUsecase.replaceProcessModel(TEST_PROJECT_ID, TEST_OLD_PROCESS_ID,
						REPLACED_PROCESS_NAME, TEST_PROCESS_XML, TEST_DESCRIPTION, false,
						null, null));

		assertEquals("CantReplaceWithCollaborationException", exception.getExceptionType());
		assertTrue(exception.getMessage().contains(TEST_OLD_PROCESS_ID.toString()));
	}

	@Test
	void testGetProcessInformation() {
		List<ProcessInformation> processInformationList = List.of(new ProcessInformation());

		when(repository.getProcessInformation(TEST_PROJECT_ID, null)).thenReturn(processInformationList);

		List<ProcessInformation> result = processModelUsecase.getProcessInformation(TEST_PROJECT_ID, null);

		assertEquals(processInformationList, result);
		verify(repository, times(1)).getProcessInformation(TEST_PROJECT_ID, null);
	}

	@Test
	void testGetProcessDetails() {
		ProcessDetails processDetails = new ProcessDetails();

		when(repository.getProcessDetails(TEST_PROCESS_MODEL_ID, false)).thenReturn(processDetails);

		ProcessDetails result = processModelUsecase.getProcessDetails(TEST_PROCESS_MODEL_ID, false);

		assertEquals(processDetails, result);
		verify(repository, times(1)).getProcessDetails(TEST_PROCESS_MODEL_ID, false);
	}

	@Test
	void testGetProcessDetails_AggregationOfEventsAndCallActivities() {
		ProcessDetails processDetails = new ProcessDetails();

		when(repository.getProcessDetails(TEST_PROCESS_MODEL_ID, true)).thenReturn(processDetails);

		ProcessDetails result = processModelUsecase.getProcessDetails(TEST_PROCESS_MODEL_ID, true);

		assertEquals(processDetails, result);
		verify(repository, times(1)).getProcessDetails(TEST_PROCESS_MODEL_ID, true);
	}

	@Test
	void testSaveProcessModel_Collaboration()
			throws CantReplaceWithCollaborationException {
		when(processOperations.getBpmnProcessId(TEST_PROCESS_XML)).thenReturn(TEST_BPMN_PROCESS_ID);
		when(repository.findByNameOrBpmnProcessIdWithoutCollaborations(TEST_PROCESS_NAME, TEST_BPMN_PROCESS_ID,
				TEST_PROJECT_ID)).thenReturn(
				null);

		when(processOperations.addEmptyProcessRefs(TEST_PROCESS_XML)).thenReturn(TEST_PROCESS_XML);

		// CREATE PROCESS MODEL
		when(processOperations.getStartEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateThrowEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateCatchEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getEndEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getCallActivities(TEST_PROCESS_XML)).thenReturn(EMPTY_CALL_ACTIVITIES);
		when(processOperations.getDataStores(TEST_PROCESS_XML)).thenReturn(EMPTY_DATA_STORES);
		when(processOperations.getDescription(TEST_PROCESS_XML)).thenReturn(TEST_DESCRIPTION);

		ProcessModel collaborationModel = new ProcessModel(TEST_PROCESS_NAME, TEST_PROCESS_XML, EMPTY_PROCESS_EVENTS,
				EMPTY_CALL_ACTIVITIES, EMPTY_DATA_STORES, TEST_DESCRIPTION,
				TEST_BPMN_PROCESS_ID, null, ProcessType.COLLABORATION);
		//

		ParticipantDetails participantDetails = new ParticipantDetails();
		participantDetails.setName(PARTICIPANT_NAME_1);
		participantDetails.setXml(PARTICIPANT_XML_1);
		participantDetails.setDescription(PARTICIPANT_DESCRIPTION_1);

		when(processOperations.getParticipants(TEST_PROCESS_XML)).thenReturn(List.of(participantDetails));

		when(repository.saveProcessModel(TEST_PROJECT_ID, collaborationModel)).thenReturn(
				TEST_PROCESS_MODEL_ID);

		// SAVE PARTICIPANT
		when(processOperations.getBpmnProcessId(PARTICIPANT_XML_1)).thenReturn(PARTICIPANT_BPMN_ID);
		when(repository.findByNameOrBpmnProcessIdWithoutCollaborations(PARTICIPANT_NAME_1, PARTICIPANT_BPMN_ID,
				TEST_PROJECT_ID)).thenReturn(
				null);

		// CREATE PARTICIPANT MODEL
		when(processOperations.getStartEvents(PARTICIPANT_XML_1)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateThrowEvents(PARTICIPANT_XML_1)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateCatchEvents(PARTICIPANT_XML_1)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getEndEvents(PARTICIPANT_XML_1)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getCallActivities(PARTICIPANT_XML_1)).thenReturn(EMPTY_CALL_ACTIVITIES);
		when(processOperations.getDataStores(PARTICIPANT_XML_1)).thenReturn(EMPTY_DATA_STORES);
		when(processOperations.getDescription(PARTICIPANT_XML_1)).thenReturn(PARTICIPANT_DESCRIPTION_1);

		ProcessModel participantModel = new ProcessModel(PARTICIPANT_NAME_1, PARTICIPANT_XML_1, EMPTY_PROCESS_EVENTS,
				EMPTY_CALL_ACTIVITIES, EMPTY_DATA_STORES, PARTICIPANT_DESCRIPTION_1,
				PARTICIPANT_BPMN_ID, TEST_BPMN_PROCESS_ID, ProcessType.PARTICIPANT);
		//

		when(repository.saveProcessModel(TEST_PROJECT_ID, participantModel)).thenReturn(PARTICIPANT_ID_1);

		ProcessModelTable participantProcessDetails = new ProcessModelTable();
		participantProcessDetails.setId(PARTICIPANT_ID_1);
		participantProcessDetails.setBpmnProcessId(PARTICIPANT_BPMN_ID);

		when(repository.getProcessModel(PARTICIPANT_ID_1)).thenReturn(participantProcessDetails);

		List<MessageFlowDetails> messageFlowDetailsList = new ArrayList<>();
		Map<String, Long> bpmnIdToIdMap = new HashMap<>();
		bpmnIdToIdMap.put(PARTICIPANT_BPMN_ID, PARTICIPANT_ID_1);
		when(processOperations.getMessageFlows(TEST_PROCESS_XML, bpmnIdToIdMap)).thenReturn(
				messageFlowDetailsList);
		doNothing().when(repository).saveMessageFlows(messageFlowDetailsList, TEST_PROJECT_ID);
		//

		Long result = processModelUsecase.saveProcessModel(TEST_PROJECT_ID, TEST_PROCESS_NAME, TEST_PROCESS_XML,
				null, IS_COLLABORATION);

		assertNotNull(result);
		assertEquals(TEST_PROCESS_MODEL_ID, result);

		verify(processOperations, times(5)).getBpmnProcessId(any(String.class));
		verify(processOperations, times(2)).getBpmnProcessId(TEST_PROCESS_XML);
		verify(processOperations, times(3)).getBpmnProcessId(PARTICIPANT_XML_1);

		verify(repository, times(3)).findByNameOrBpmnProcessIdWithoutCollaborations(any(String.class),
				any(String.class), any(Long.class));
		verify(repository, times(1)).findByNameOrBpmnProcessIdWithoutCollaborations(TEST_PROCESS_NAME,
				TEST_BPMN_PROCESS_ID,
				TEST_PROJECT_ID);
		verify(repository, times(2)).findByNameOrBpmnProcessIdWithoutCollaborations(PARTICIPANT_NAME_1,
				PARTICIPANT_BPMN_ID,
				TEST_PROJECT_ID);

		verify(processOperations, times(1)).addEmptyProcessRefs(TEST_PROCESS_XML);

		verify(processOperations, times(2)).getStartEvents(any(String.class));
		verify(processOperations, times(1)).getStartEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getStartEvents(PARTICIPANT_XML_1);

		verify(processOperations, times(2)).getIntermediateThrowEvents(any(String.class));
		verify(processOperations, times(1)).getIntermediateThrowEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getIntermediateThrowEvents(PARTICIPANT_XML_1);

		verify(processOperations, times(2)).getIntermediateCatchEvents(any(String.class));
		verify(processOperations, times(1)).getIntermediateCatchEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getIntermediateCatchEvents(PARTICIPANT_XML_1);

		verify(processOperations, times(2)).getEndEvents(any(String.class));
		verify(processOperations, times(1)).getEndEvents(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getEndEvents(PARTICIPANT_XML_1);

		verify(processOperations, times(2)).getCallActivities(any(String.class));
		verify(processOperations, times(1)).getCallActivities(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getCallActivities(PARTICIPANT_XML_1);

		verify(processOperations, times(2)).getDataStores(any(String.class));
		verify(processOperations, times(1)).getDataStores(TEST_PROCESS_XML);
		verify(processOperations, times(1)).getDataStores(PARTICIPANT_XML_1);

		verify(processOperations, times(1)).getDescription(TEST_PROCESS_XML);

		verify(processOperations, times(1)).getParticipants(TEST_PROCESS_XML);

		verify(repository, times(2)).saveProcessModel(any(Long.class), any(ProcessModel.class));
		verify(repository, times(1)).saveProcessModel(TEST_PROJECT_ID, collaborationModel);
		verify(repository, times(1)).saveProcessModel(TEST_PROJECT_ID, participantModel);

		verify(processOperations, times(1)).getMessageFlows(TEST_PROCESS_XML, bpmnIdToIdMap);
		verify(repository, times(1)).saveMessageFlows(messageFlowDetailsList, TEST_PROJECT_ID);

		verifyNoMoreInteractions(processOperations);
		verifyNoMoreInteractions(repository);
	}

	@Test
	void testSaveProcessModel_ReplaceParticipant()
			throws CantReplaceWithCollaborationException {
		when(processOperations.getBpmnProcessId(TEST_PROCESS_XML)).thenReturn(TEST_BPMN_PROCESS_ID);
		when(repository.findByNameOrBpmnProcessIdWithoutCollaborations(TEST_PROCESS_NAME, TEST_BPMN_PROCESS_ID,
				TEST_PROJECT_ID)).thenReturn(
				null);
		when(processOperations.addEmptyProcessRefs(TEST_PROCESS_XML)).thenReturn(TEST_PROCESS_XML);

		// CREATE PROCESS MODEL
		when(processOperations.getStartEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateThrowEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateCatchEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getEndEvents(TEST_PROCESS_XML)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getCallActivities(TEST_PROCESS_XML)).thenReturn(EMPTY_CALL_ACTIVITIES);
		when(processOperations.getDataStores(TEST_PROCESS_XML)).thenReturn(EMPTY_DATA_STORES);
		when(processOperations.getDescription(TEST_PROCESS_XML)).thenReturn(TEST_DESCRIPTION);

		ProcessModel collaborationModel = new ProcessModel(TEST_PROCESS_NAME, TEST_PROCESS_XML, EMPTY_PROCESS_EVENTS,
				EMPTY_CALL_ACTIVITIES, EMPTY_DATA_STORES, TEST_DESCRIPTION,
				TEST_BPMN_PROCESS_ID, null, ProcessType.COLLABORATION);
		//

		ParticipantDetails participant1 = new ParticipantDetails();
		participant1.setName(PARTICIPANT_NAME_2);
		participant1.setDescription(PARTICIPANT_DESCRIPTION_1);
		participant1.setXml(PARTICIPANT_XML_2);

		ParticipantDetails participant2 = new ParticipantDetails();
		participant2.setName(PARTICIPANT_NAME_1);
		participant2.setDescription(PARTICIPANT_DESCRIPTION_1);
		participant2.setXml(PARTICIPANT_XML_1);

		ParticipantDetails participant3 = new ParticipantDetails();
		participant3.setName(PARTICIPANT_NAME_1);
		participant3.setDescription(PARTICIPANT_DESCRIPTION_1);
		participant3.setXml(PARTICIPANT_XML_1);

		List<ParticipantDetails> participants = List.of(participant1, participant2, participant3);
		when(processOperations.getParticipants(TEST_PROCESS_XML)).thenReturn(participants);

		when(repository.saveProcessModel(TEST_PROJECT_ID, collaborationModel)).thenReturn(TEST_PROCESS_MODEL_ID);

		when(processOperations.getBpmnProcessId(PARTICIPANT_XML_1)).thenReturn(PARTICIPANT_BPMN_ID);
		when(processOperations.getBpmnProcessId(PARTICIPANT_XML_2)).thenReturn(PARTICIPANT_BPMN_ID_2);

		ProcessModelTable existingParticipant = new ProcessModelTable();
		existingParticipant.setId(PARTICIPANT_ID_1);
		existingParticipant.setProcessType(ProcessType.PARTICIPANT);
		when(repository.findByNameOrBpmnProcessIdWithoutCollaborations(PARTICIPANT_NAME_1, PARTICIPANT_BPMN_ID,
				TEST_PROJECT_ID))
				.thenReturn(null)
				.thenReturn(null)
				.thenReturn(existingParticipant);
		when(repository.findByNameOrBpmnProcessIdWithoutCollaborations(PARTICIPANT_NAME_2, PARTICIPANT_BPMN_ID_2,
				TEST_PROJECT_ID)).thenReturn(
				null);

		// CREATE PARTICIPANT
		when(processOperations.getStartEvents(PARTICIPANT_XML_1)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateThrowEvents(PARTICIPANT_XML_1)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateCatchEvents(PARTICIPANT_XML_1)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getEndEvents(PARTICIPANT_XML_1)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getCallActivities(PARTICIPANT_XML_1)).thenReturn(EMPTY_CALL_ACTIVITIES);
		when(processOperations.getDataStores(PARTICIPANT_XML_1)).thenReturn(EMPTY_DATA_STORES);
		when(processOperations.getDescription(PARTICIPANT_XML_1)).thenReturn(PARTICIPANT_DESCRIPTION_1);

		when(processOperations.getStartEvents(PARTICIPANT_XML_2)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateThrowEvents(PARTICIPANT_XML_2)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getIntermediateCatchEvents(PARTICIPANT_XML_2)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getEndEvents(PARTICIPANT_XML_2)).thenReturn(EMPTY_PROCESS_EVENTS);
		when(processOperations.getCallActivities(PARTICIPANT_XML_2)).thenReturn(EMPTY_CALL_ACTIVITIES);
		when(processOperations.getDataStores(PARTICIPANT_XML_2)).thenReturn(EMPTY_DATA_STORES);
		when(processOperations.getDescription(PARTICIPANT_XML_2)).thenReturn(PARTICIPANT_DESCRIPTION_1);

		ProcessModel participantModel1 = new ProcessModel(PARTICIPANT_NAME_2, PARTICIPANT_XML_2, EMPTY_PROCESS_EVENTS,
				EMPTY_CALL_ACTIVITIES, EMPTY_DATA_STORES, PARTICIPANT_DESCRIPTION_1,
				PARTICIPANT_BPMN_ID_2, TEST_BPMN_PROCESS_ID, ProcessType.PARTICIPANT);

		ProcessModel participantModel2 = new ProcessModel(PARTICIPANT_NAME_1, PARTICIPANT_XML_1, EMPTY_PROCESS_EVENTS,
				EMPTY_CALL_ACTIVITIES, EMPTY_DATA_STORES, PARTICIPANT_DESCRIPTION_1,
				PARTICIPANT_BPMN_ID, TEST_BPMN_PROCESS_ID, ProcessType.PARTICIPANT);
		//

		when(repository.saveProcessModel(TEST_PROJECT_ID, participantModel2))
				.thenReturn(PARTICIPANT_ID_1)
				.thenReturn(PARTICIPANT_ID_2);

		when(repository.saveProcessModel(TEST_PROJECT_ID, participantModel1))
				.thenReturn(PARTICIPANT_ID_3);

		// REPLACE PARTICIPANT
		doNothing().when(processMapRepository).copyConnections(TEST_PROJECT_ID, PARTICIPANT_ID_1, PARTICIPANT_ID_2);
		doNothing().when(processMapRepository)
				.copyMessageFlowsAndRelations(TEST_PROJECT_ID, PARTICIPANT_ID_1, PARTICIPANT_ID_2);
		doNothing().when(repository).deleteProcessModel(PARTICIPANT_ID_1);
		//

		Map<String, Long> bpmnIdToIdMap = new HashMap<>();
		bpmnIdToIdMap.put(PARTICIPANT_BPMN_ID_2, PARTICIPANT_ID_3);
		bpmnIdToIdMap.put(PARTICIPANT_BPMN_ID, PARTICIPANT_ID_2);
		when(processOperations.getMessageFlows(TEST_PROCESS_XML, bpmnIdToIdMap)).thenReturn(EMPTY_MESSAGE_FLOWS);

		Long result = processModelUsecase.saveProcessModel(TEST_PROJECT_ID, TEST_PROCESS_NAME, TEST_PROCESS_XML,
				null, IS_COLLABORATION);

		assertNotNull(result);
		assertEquals(TEST_PROCESS_MODEL_ID, result);

		verify(processOperations, times(11)).getBpmnProcessId(any(String.class));
		verify(processOperations, times(2)).getBpmnProcessId(TEST_PROCESS_XML);
		verify(processOperations, times(6)).getBpmnProcessId(PARTICIPANT_XML_1);
		verify(processOperations, times(3)).getBpmnProcessId(PARTICIPANT_XML_2);

		verify(repository, times(7)).findByNameOrBpmnProcessIdWithoutCollaborations(any(String.class),
				any(String.class), any(Long.class));
		verify(repository, times(1)).findByNameOrBpmnProcessIdWithoutCollaborations(TEST_PROCESS_NAME,
				TEST_BPMN_PROCESS_ID,
				TEST_PROJECT_ID);
		verify(repository, times(4)).findByNameOrBpmnProcessIdWithoutCollaborations(PARTICIPANT_NAME_1,
				PARTICIPANT_BPMN_ID,
				TEST_PROJECT_ID);
		verify(repository, times(2)).findByNameOrBpmnProcessIdWithoutCollaborations(PARTICIPANT_NAME_2,
				PARTICIPANT_BPMN_ID_2,
				TEST_PROJECT_ID);

		verify(processOperations, times(1)).addEmptyProcessRefs(TEST_PROCESS_XML);

		verify(processOperations, times(4)).getStartEvents(any(String.class));
		verify(processOperations, times(1)).getStartEvents(TEST_PROCESS_XML);
		verify(processOperations, times(2)).getStartEvents(PARTICIPANT_XML_1);
		verify(processOperations, times(1)).getStartEvents(PARTICIPANT_XML_2);

		verify(processOperations, times(4)).getIntermediateThrowEvents(any(String.class));
		verify(processOperations, times(1)).getIntermediateThrowEvents(TEST_PROCESS_XML);
		verify(processOperations, times(2)).getIntermediateThrowEvents(PARTICIPANT_XML_1);
		verify(processOperations, times(1)).getIntermediateThrowEvents(PARTICIPANT_XML_2);

		verify(processOperations, times(4)).getIntermediateCatchEvents(any(String.class));
		verify(processOperations, times(1)).getIntermediateCatchEvents(TEST_PROCESS_XML);
		verify(processOperations, times(2)).getIntermediateCatchEvents(PARTICIPANT_XML_1);
		verify(processOperations, times(1)).getIntermediateCatchEvents(PARTICIPANT_XML_2);

		verify(processOperations, times(4)).getEndEvents(any(String.class));
		verify(processOperations, times(1)).getEndEvents(TEST_PROCESS_XML);
		verify(processOperations, times(2)).getEndEvents(PARTICIPANT_XML_1);
		verify(processOperations, times(1)).getEndEvents(PARTICIPANT_XML_2);

		verify(processOperations, times(4)).getCallActivities(any(String.class));
		verify(processOperations, times(1)).getCallActivities(TEST_PROCESS_XML);
		verify(processOperations, times(2)).getCallActivities(PARTICIPANT_XML_1);
		verify(processOperations, times(1)).getCallActivities(PARTICIPANT_XML_2);

		verify(processOperations, times(4)).getDataStores(any(String.class));
		verify(processOperations, times(1)).getDataStores(TEST_PROCESS_XML);
		verify(processOperations, times(2)).getDataStores(PARTICIPANT_XML_1);
		verify(processOperations, times(1)).getDataStores(PARTICIPANT_XML_2);

		verify(processOperations, times(1)).getDescription(TEST_PROCESS_XML);

		verify(processOperations, times(1)).getParticipants(TEST_PROCESS_XML);

		verify(repository, times(4)).saveProcessModel(any(Long.class), any(ProcessModel.class));
		verify(repository, times(1)).saveProcessModel(TEST_PROJECT_ID, collaborationModel);
		verify(repository, times(2)).saveProcessModel(TEST_PROJECT_ID, participantModel2);
		verify(repository, times(1)).saveProcessModel(TEST_PROJECT_ID, participantModel1);

		verify(processOperations, times(1)).getMessageFlows(TEST_PROCESS_XML, bpmnIdToIdMap);
		verify(repository, times(1)).saveMessageFlows(EMPTY_MESSAGE_FLOWS, TEST_PROJECT_ID);

		verify(processMapRepository, times(1)).copyConnections(TEST_PROJECT_ID, PARTICIPANT_ID_1, PARTICIPANT_ID_2);
		verify(processMapRepository, times(1)).copyMessageFlowsAndRelations(TEST_PROJECT_ID, PARTICIPANT_ID_1,
				PARTICIPANT_ID_2);
		verify(repository, times(1)).deleteProcessModel(PARTICIPANT_ID_1);

		verifyNoMoreInteractions(processOperations);
		verifyNoMoreInteractions(repository);
		verifyNoMoreInteractions(processMapRepository);
	}
}