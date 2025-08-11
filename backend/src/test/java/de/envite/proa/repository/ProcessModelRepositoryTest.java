package de.envite.proa.repository;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.datastore.DataAccess;
import de.envite.proa.entities.process.*;
import de.envite.proa.repository.datastore.DataStoreConnectionDao;
import de.envite.proa.repository.datastore.DataStoreDao;
import de.envite.proa.repository.messageflow.MessageFlowDao;
import de.envite.proa.repository.processmodel.*;
import de.envite.proa.repository.tables.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProcessModelRepositoryTest {

	private static final String PROCESS_DESCRIPTION = "Description";
	private static final String COMMON_EVENT_LABEL = "common event label";
	private static final String EXISTING_PROCESS_MODEL_NAME = "TestProcessModel";
	private static final String NEW_PROCESS_MODEL_NAME = "NewTestProcessModel";
	private static final String EXISTING_EVENT_ID = "existingEventID";
	private static final String NEW_EVENT_ID = "newEventID";
	private static final String NEW_ACTIVITY_ID = "newActivityId";
	private static final String EXISTING_ACTIVITY_ID = "existingActivityId";
	private static final String DATA_STORE_LABEL = "DataStoreLabel";
	private static final String DIFFERENT_DATA_STORE_LABEL = "OtherDifferentDataStoreLabel";
	private static final Long PROCESS_MODEL_ID_1 = 1L;
	private static final Long PROCESS_MODEL_ID_2 = 2L;
	private static final Long PROCESS_MODEL_ID_3 = 3L;
	private static final Long PROCESS_MODEL_ID_4 = 4L;
	private static final Long PROCESS_MODEL_ID_5 = 5L;
	private static final Long PROJECT_ID = 2341234L;
	private static final String BPMN_PROCESS_ID = "bpmn123";
	private static final byte[] BPMN_XML = "<xml></xml>".getBytes();
	private static final String PARENT_PROCESS_MODEL_ID = "parentProcessModelId";
	private static final String PROCESS_MODEL_NAME = "processModelName";
	private static final String PROCESS_EVENT_ID = "processEventId";
	private static final String PROCESS_EVENT_LABEL = "processEventName";
	private static final String DIFFERENT_EVENT_LABEL = "differentEventLabel";

	@InjectMocks
	private ProcessmodelRepositoryImpl repository;
	@Mock
	private ProcessModelDao processModelDao;
	@Mock
	private DataStoreDao dataStoreDao;
	@Mock
	private DataStoreConnectionDao dataStoreConnectionDao;
	@Mock
	private CallActivityDao callActivityDao;
	@Mock
	private ProcessConnectionDao processConnectionDao;
	@Mock
	private ProcessEventDao processEventDao;
	@Mock
	private MessageFlowDao messageFlowDao;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testEndEvent() {

		// Arrange
		ProjectTable project = new ProjectTable();
		project.setId(PROJECT_ID);

		ProcessEventTable processEventTable = new ProcessEventTable();
		processEventTable.setEventType(EventType.START);
		processEventTable.setLabel(COMMON_EVENT_LABEL);
		processEventTable.setElementId(EXISTING_EVENT_ID);
		ProcessModelTable processModelTable = new ProcessModelTable();
		processModelTable.setName(EXISTING_PROCESS_MODEL_NAME);
		processEventTable.setProcessModel(processModelTable);
		List<ProcessEventTable> startEventTables = List.of(processEventTable);
		when(processEventDao.getEvents(project)).thenReturn(startEventTables);

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				processModelDao, //
				dataStoreDao, //
				dataStoreConnectionDao, //
				callActivityDao, //
				processConnectionDao, //
				processEventDao, //
				messageFlowDao);

		ProcessModel model = new ProcessModel();
		model.setName(NEW_PROCESS_MODEL_NAME);

		ProcessEvent endEvent = new ProcessEvent();
		endEvent.setElementId(NEW_EVENT_ID);
		endEvent.setLabel(COMMON_EVENT_LABEL);
		endEvent.setEventType(EventType.END);

		model.setEvents(Set.of(endEvent));

		// Act
		repository.saveProcessModel(PROJECT_ID, model);

		// Assert
		ArgumentCaptor<ProcessConnectionTable> connectionCaptor = ArgumentCaptor.forClass(ProcessConnectionTable.class);
		verify(processConnectionDao).persist(connectionCaptor.capture());
		verify(processEventDao).getEvents(project);
		verifyNoMoreInteractions(processConnectionDao);
		verifyNoMoreInteractions(processEventDao);
		ProcessConnectionTable connection = connectionCaptor.getValue();

		assertThat(connection.getCalledProcess().getName()).isEqualTo(EXISTING_PROCESS_MODEL_NAME);
		assertThat(connection.getCalledElement()).isEqualTo(EXISTING_EVENT_ID);
		assertThat(connection.getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);

		assertThat(connection.getCallingProcess().getName()).isEqualTo(NEW_PROCESS_MODEL_NAME);
		assertThat(connection.getCallingElement()).isEqualTo(NEW_EVENT_ID);
		assertThat(connection.getCallingElementType()).isEqualTo(ProcessElementType.END_EVENT);

		assertThat(connection.getLabel()).isEqualTo(COMMON_EVENT_LABEL);

		assertThat(connection.getUserCreated()).isFalse();
	}

	@Test
	void testStartEvent() {

		// Arrange
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		ProcessEventTable processEventTable = new ProcessEventTable();
		processEventTable.setEventType(EventType.END);
		processEventTable.setLabel(COMMON_EVENT_LABEL);
		processEventTable.setElementId(EXISTING_EVENT_ID);
		ProcessModelTable processModelTable = new ProcessModelTable();
		processModelTable.setName(EXISTING_PROCESS_MODEL_NAME);
		processEventTable.setProcessModel(processModelTable);
		List<ProcessEventTable> endEventTables = List.of(processEventTable);
		when(processEventDao.getEvents(projectTable)).thenReturn(endEventTables);

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				processModelDao, //
				dataStoreDao, //
				dataStoreConnectionDao, //
				callActivityDao, //
				processConnectionDao, //
				processEventDao, //
				messageFlowDao);

		ProcessModel model = new ProcessModel();
		model.setName(NEW_PROCESS_MODEL_NAME);
		ProcessEvent startEvent = new ProcessEvent();
		startEvent.setElementId(NEW_EVENT_ID);
		startEvent.setLabel(COMMON_EVENT_LABEL);
		startEvent.setEventType(EventType.START);

		model.setEvents(Set.of(startEvent));

		// Act
		repository.saveProcessModel(PROJECT_ID, model);

		// Assert
		ArgumentCaptor<ProcessConnectionTable> connectionCaptor = ArgumentCaptor.forClass(ProcessConnectionTable.class);
		verify(processConnectionDao).persist(connectionCaptor.capture());
		ProcessConnectionTable connection = connectionCaptor.getValue();

		assertThat(connection.getCalledProcess().getName()).isEqualTo(NEW_PROCESS_MODEL_NAME);
		assertThat(connection.getCalledElement()).isEqualTo(NEW_EVENT_ID);
		assertThat(connection.getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);

		assertThat(connection.getCallingProcess().getName()).isEqualTo(EXISTING_PROCESS_MODEL_NAME);
		assertThat(connection.getCallingElement()).isEqualTo(EXISTING_EVENT_ID);
		assertThat(connection.getCallingElementType()).isEqualTo(ProcessElementType.END_EVENT);

		assertThat(connection.getLabel()).isEqualTo(COMMON_EVENT_LABEL);

		assertThat(connection.getUserCreated()).isFalse();
	}

	@Test
	void testCallActivity() {

		// Arrange
		ProjectTable project = new ProjectTable();
		project.setId(PROJECT_ID);
		List<CallActivityTable> emptyCallActivities = new ArrayList<>();
		ProcessModelTable processModelTable = new ProcessModelTable();
		processModelTable.setName(EXISTING_PROCESS_MODEL_NAME);
		when(processModelDao.getProcessModels(project)).thenReturn(List.of(processModelTable));
		when(callActivityDao.getCallActivities(project)).thenReturn(emptyCallActivities);

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				processModelDao, //
				dataStoreDao, //
				dataStoreConnectionDao, //
				callActivityDao, //
				processConnectionDao, //
				processEventDao, //
				messageFlowDao);

		ProcessModel model = new ProcessModel();
		model.setName(NEW_PROCESS_MODEL_NAME);

		ProcessActivity activity = new ProcessActivity();
		activity.setElementId(NEW_ACTIVITY_ID);
		activity.setLabel(EXISTING_PROCESS_MODEL_NAME);

		model.setCallActivities(Set.of(activity));

		// Act
		repository.saveProcessModel(PROJECT_ID, model);

		// Assert
		ArgumentCaptor<ProcessConnectionTable> connectionCaptor = ArgumentCaptor.forClass(ProcessConnectionTable.class);
		ArgumentCaptor<ProcessModelTable> processModelCaptor = ArgumentCaptor.forClass(ProcessModelTable.class);
		verify(processConnectionDao).persist(connectionCaptor.capture());
		verify(processModelDao).getProcessModels(project);
		verify(processModelDao).persist(processModelCaptor.capture());
		verify(callActivityDao).getCallActivities(project);
		verifyNoMoreInteractions(processConnectionDao);
		verifyNoMoreInteractions(processModelDao);
		verifyNoMoreInteractions(callActivityDao);
		ProcessConnectionTable connection = connectionCaptor.getValue();

		assertThat(connection.getCallingProcess().getName()).isEqualTo(NEW_PROCESS_MODEL_NAME);
		assertThat(connection.getCallingElement()).isEqualTo(NEW_ACTIVITY_ID);
		assertThat(connection.getCallingElementType()).isEqualTo(ProcessElementType.CALL_ACTIVITY);

		assertThat(connection.getCalledProcess().getName()).isEqualTo(EXISTING_PROCESS_MODEL_NAME);
		assertThat(connection.getCalledElement()).isNull();
		assertThat(connection.getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);

		assertThat(connection.getLabel()).isEqualTo(EXISTING_PROCESS_MODEL_NAME);

		assertThat(connection.getUserCreated()).isFalse();
	}

	@Test
	void testProcessCalledByActivity() {

		// Arrange
		ProjectTable project = new ProjectTable();
		project.setId(PROJECT_ID);

		ProcessModelTable processModelTable = new ProcessModelTable();
		processModelTable.setName(EXISTING_PROCESS_MODEL_NAME);

		CallActivityTable callActivityTable = new CallActivityTable();
		callActivityTable.setElementId(EXISTING_ACTIVITY_ID);
		callActivityTable.setLabel(NEW_PROCESS_MODEL_NAME);
		callActivityTable.setProcessModel(processModelTable);

		when(callActivityDao.getCallActivities(project)).thenReturn(List.of(callActivityTable));

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				processModelDao, //
				dataStoreDao, //
				dataStoreConnectionDao, //
				callActivityDao, //
				processConnectionDao, //
				processEventDao, //
				messageFlowDao);

		ProcessModel model = new ProcessModel();
		model.setName(NEW_PROCESS_MODEL_NAME);

		// Act
		repository.saveProcessModel(PROJECT_ID, model);

		// Assert
		ArgumentCaptor<ProcessConnectionTable> connectionCaptor = ArgumentCaptor.forClass(ProcessConnectionTable.class);
		verify(processConnectionDao).persist(connectionCaptor.capture());
		verify(callActivityDao).getCallActivities(project);
		verifyNoMoreInteractions(processConnectionDao);
		verifyNoMoreInteractions(callActivityDao);
		ProcessConnectionTable connection = connectionCaptor.getValue();

		assertThat(connection.getCallingProcess().getName()).isEqualTo(EXISTING_PROCESS_MODEL_NAME);
		assertThat(connection.getCallingElement()).isEqualTo(EXISTING_ACTIVITY_ID);
		assertThat(connection.getCallingElementType()).isEqualTo(ProcessElementType.CALL_ACTIVITY);

		assertThat(connection.getCalledProcess().getName()).isEqualTo(NEW_PROCESS_MODEL_NAME);
		assertThat(connection.getCalledElement()).isNull();
		assertThat(connection.getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);

		assertThat(connection.getLabel()).isEqualTo(NEW_PROCESS_MODEL_NAME);

		assertThat(connection.getUserCreated()).isFalse();
	}

	@Test
	void testDataStore() {
		// Arrange
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		DataStoreTable dataStoreTable = new DataStoreTable();
		dataStoreTable.setLabel(DATA_STORE_LABEL);

		when(dataStoreDao.getDataStores(projectTable)).thenReturn(List.of(dataStoreTable));

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				processModelDao, //
				dataStoreDao, //
				dataStoreConnectionDao, //
				callActivityDao, //
				processConnectionDao, //
				processEventDao, //
				messageFlowDao);

		ProcessModel model = new ProcessModel();
		model.setName(NEW_PROCESS_MODEL_NAME);

		ProcessDataStore dataStore = new ProcessDataStore();
		dataStore.setLabel(DATA_STORE_LABEL);
		dataStore.setAccess(DataAccess.READ_WRITE);

		model.setDataStores(List.of(dataStore));

		// Act
		repository.saveProcessModel(PROJECT_ID, model);

		// Assert
		ArgumentCaptor<DataStoreConnectionTable> connectionCaptor = ArgumentCaptor
				.forClass(DataStoreConnectionTable.class);
		verify(dataStoreConnectionDao).persist(connectionCaptor.capture());
		DataStoreConnectionTable connection = connectionCaptor.getValue();

		assertThat(connection.getProcess().getName()).isEqualTo(NEW_PROCESS_MODEL_NAME);
		assertThat(connection.getDataStore().getLabel()).isEqualTo(DATA_STORE_LABEL);
		assertThat(connection.getAccess()).isEqualTo(DataAccess.READ_WRITE);

		verify(dataStoreDao, times(1)).getDataStores(projectTable);
		verifyNoMoreInteractions(dataStoreDao);

		verifyNoMoreInteractions(dataStoreConnectionDao);
	}

	@Test
	void testGetProcessInformation() {

		// Arrange
		LocalDateTime dateTime = LocalDateTime.now();

		ProcessModelTable processModel = new ProcessModelTable();
		processModel.setId(PROCESS_MODEL_ID_1);
		processModel.setName(EXISTING_PROCESS_MODEL_NAME);
		processModel.setDescription(PROCESS_DESCRIPTION);
		processModel.setCreatedAt(dateTime);

		when(processModelDao.getProcessModelsWithChildren(any())).thenReturn(List.of(processModel));

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				processModelDao, //
				dataStoreDao, //
				dataStoreConnectionDao, //
				callActivityDao, //
				processConnectionDao, //
				processEventDao, //
				messageFlowDao);

		// Act
		List<ProcessInformation> processInformation = repository.getProcessInformation(PROJECT_ID);

		// Assert
		assertThat(processInformation)//
				.hasSize(1)//
				.extracting("id", "processName", "description", "createdAt")//
				.contains(tuple(PROCESS_MODEL_ID_1, EXISTING_PROCESS_MODEL_NAME, PROCESS_DESCRIPTION, dateTime));

		ArgumentCaptor<ProjectTable> projectCaptor = ArgumentCaptor.forClass(ProjectTable.class);
		verify(processModelDao, times(1)).getProcessModelsWithChildren(projectCaptor.capture());
		assertThat(projectCaptor.getValue().getId()).isEqualTo(PROJECT_ID);

	}

	@Test
	public void testFindByNameOrBpmnProcessIdWithoutCollaborations() {
		ProcessModelTable process = new ProcessModelTable();
		ProjectTable project = new ProjectTable();
		project.setId(PROJECT_ID);

		when(processModelDao.findByNameOrBpmnProcessIdWithoutCollaborations(NEW_PROCESS_MODEL_NAME, BPMN_PROCESS_ID,
				project)).thenReturn(process);

		ProcessModelTable result = repository.findByNameOrBpmnProcessIdWithoutCollaborations(NEW_PROCESS_MODEL_NAME,
				BPMN_PROCESS_ID,
				PROJECT_ID);

		assertThat(result).isEqualTo(process);

		verify(processModelDao, times(1)).findByNameOrBpmnProcessIdWithoutCollaborations(NEW_PROCESS_MODEL_NAME,
				BPMN_PROCESS_ID, project);
	}

	@Test
	public void testGetProcessModel() {
		ProcessModelTable processModel = new ProcessModelTable();

		when(processModelDao.find(PROCESS_MODEL_ID_1)).thenReturn(processModel);

		ProcessModelTable result = repository.getProcessModel(PROCESS_MODEL_ID_1);

		assertEquals(processModel, result);
		verify(processModelDao, times(1)).find(PROCESS_MODEL_ID_1);
	}

	@Test
	public void testGetProcessModelXml() {
		when(processModelDao.getBpmnXml(PROCESS_MODEL_ID_1)).thenReturn(BPMN_XML);

		String result = repository.getProcessModelXml(PROCESS_MODEL_ID_1);

		assertThat(result.getBytes()).isEqualTo(BPMN_XML);
		verify(processModelDao, times(1)).getBpmnXml(PROCESS_MODEL_ID_1);
	}

	@Test
	public void testSaveMessageFlows() {
		List<String> messageFlowIds = List.of("1", "2", "3");
		List<MessageFlowDetails> messageFlowDetails = new ArrayList<>();
		for (String messageFlowId : messageFlowIds) {
			MessageFlowDetails details = new MessageFlowDetails();
			details.setBpmnId(messageFlowId);
			messageFlowDetails.add(details);
		}

		repository.saveMessageFlows(messageFlowDetails, PROJECT_ID);

		verify(messageFlowDao, times(3)).persist(any(MessageFlowTable.class));
	}

	@Test
	public void testSaveProcessModel_WithParent() {
		ProjectTable project = new ProjectTable();
		project.setId(PROJECT_ID);

		ProcessModel processModel = new ProcessModel();
		processModel.setName(PROCESS_MODEL_NAME);
		processModel.setParentBpmnProcessId(PARENT_PROCESS_MODEL_ID);

		ArgumentCaptor<ProcessModelTable> processModelCaptor = ArgumentCaptor.forClass(ProcessModelTable.class);
		doNothing().when(processModelDao).persist(processModelCaptor.capture());

		ProcessModelTable parent = new ProcessModelTable();
		parent.setId(PROCESS_MODEL_ID_1);
		when(processModelDao.findByBpmnProcessIdWithChildren(PARENT_PROCESS_MODEL_ID, project)).thenReturn(parent);

		when(processModelDao.getProcessModels(project)).thenReturn(new ArrayList<>());

		doNothing().when(processModelDao).merge(any(ProcessModelTable.class));

		repository.saveProcessModel(PROJECT_ID, processModel);

		verify(processModelDao, times(1)).persist(processModelCaptor.capture());

		verify(processModelDao, times(1)).findByBpmnProcessIdWithChildren(PARENT_PROCESS_MODEL_ID,
				project);
		verify(processModelDao, times(1)).addChild(PROCESS_MODEL_ID_1, processModelCaptor.getValue().getId());
		verify(processModelDao, times(1)).getProcessModels(project);
		verifyNoMoreInteractions(processModelDao);
	}

	@Test
	public void testDeleteProcessModel_DeleteParentsAndChildren() {
		ProcessModelTable processModel = new ProcessModelTable();
		processModel.setId(PROCESS_MODEL_ID_1);

		ProcessModelTable parent = new ProcessModelTable();
		parent.setId(PROCESS_MODEL_ID_2);
		parent.setChildren(Set.of(processModel));
		processModel.setParents(Set.of(parent));

		ProcessModelTable child1 = new ProcessModelTable();
		child1.setId(PROCESS_MODEL_ID_3);
		child1.setParents(Set.of(processModel));
		ProcessModelTable child2 = new ProcessModelTable();
		child2.setId(PROCESS_MODEL_ID_4);
		ProcessModelTable otherParent = new ProcessModelTable();
		otherParent.setId(PROCESS_MODEL_ID_5);
		child2.setParents(Set.of(processModel, otherParent));

		processModel.setChildren(Set.of(child1, child2));

		when(processModelDao.findWithParents(PROCESS_MODEL_ID_1)).thenReturn(processModel);
		when(processModelDao.findWithParents(PROCESS_MODEL_ID_3)).thenReturn(child1);
		when(processModelDao.findWithParents(PROCESS_MODEL_ID_4)).thenReturn(child2);

		when(processModelDao.findWithParentsAndChildren(PROCESS_MODEL_ID_1)).thenReturn(processModel);
		when(processModelDao.findWithParentsAndChildren(PROCESS_MODEL_ID_2)).thenReturn(parent);
		when(processModelDao.findWithParentsAndChildren(PROCESS_MODEL_ID_3)).thenReturn(child1);
		when(processModelDao.findWithParentsAndChildren(PROCESS_MODEL_ID_4)).thenReturn(child2);
		when(processModelDao.findWithParentsAndChildren(PROCESS_MODEL_ID_5)).thenReturn(otherParent);

		doNothing().when(dataStoreConnectionDao).deleteForProcessModel(any(Long.class));
		doNothing().when(processConnectionDao).deleteForProcessModel(any(Long.class));
		List<Long> expectedIds = List.of(PROCESS_MODEL_ID_1, PROCESS_MODEL_ID_3, PROCESS_MODEL_ID_2);
		doNothing().when(processModelDao).delete(expectedIds);

		repository.deleteProcessModel(PROCESS_MODEL_ID_1);

		verify(processModelDao, times(1)).findWithParents(PROCESS_MODEL_ID_1);
		verify(processModelDao, times(1)).findWithParents(PROCESS_MODEL_ID_3);
		verify(processModelDao, times(1)).findWithParents(PROCESS_MODEL_ID_4);

		verify(processModelDao, times(1)).findWithParentsAndChildren(PROCESS_MODEL_ID_1);
		verify(processModelDao, times(1)).findWithParentsAndChildren(PROCESS_MODEL_ID_2);
		verify(processModelDao, times(1)).findWithParentsAndChildren(PROCESS_MODEL_ID_3);

		verify(dataStoreConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_1);
		verify(dataStoreConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_3);
		verify(dataStoreConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_2);

		verify(processConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_1);
		verify(processConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_3);
		verify(processConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_2);

		verify(processModelDao, times(1)).delete(expectedIds);

		verifyNoMoreInteractions(processModelDao);
		verifyNoMoreInteractions(dataStoreConnectionDao);
		verifyNoMoreInteractions(processConnectionDao);
	}

	@Test
	public void testSaveProcessModel_UnknownEventType() {

		ProcessModel processModel = new ProcessModel();
		ProcessEvent event = new ProcessEvent();
		event.setEventType(EventType.INVALID);
		processModel.setEvents(Set.of(event));

		doNothing().when(processModelDao).persist(any(ProcessModelTable.class));

		assertThrows(IllegalArgumentException.class, () -> repository.saveProcessModel(PROJECT_ID, processModel));

		verify(processModelDao, times(1)).persist(any(ProcessModelTable.class));
	}

	@Test
	public void testSaveProcessModel_ConnectIntermediateCatchEvent() {
		ProjectTable project = new ProjectTable();
		project.setId(PROJECT_ID);

		doNothing().when(processModelDao).persist(any(ProcessModelTable.class));

		ProcessModel processModel = new ProcessModel();
		ProcessEvent event = new ProcessEvent();
		event.setElementId(PROCESS_EVENT_ID);
		event.setEventType(EventType.INTERMEDIATE_CATCH);
		event.setLabel(PROCESS_EVENT_LABEL);
		processModel.setEvents(Set.of(event));

		ProcessEventTable eventToConnect1 = new ProcessEventTable();
		eventToConnect1.setEventType(EventType.END);
		eventToConnect1.setLabel(PROCESS_EVENT_LABEL);

		ProcessEventTable eventToConnect2 = new ProcessEventTable();
		eventToConnect2.setEventType(EventType.INTERMEDIATE_THROW);
		eventToConnect2.setLabel(PROCESS_EVENT_LABEL);

		when(processEventDao.getEvents(project)).thenReturn(List.of(eventToConnect1, eventToConnect2));

		doNothing().when(processConnectionDao).persist(any(ProcessConnectionTable.class));

		repository.saveProcessModel(PROJECT_ID, processModel);

		verify(processModelDao, times(1)).persist(any(ProcessModelTable.class));

		ArgumentCaptor<ProjectTable> projectCaptor = ArgumentCaptor.forClass(ProjectTable.class);

		verify(processEventDao, times(1)).getEvents(project);
		assertThat(projectCaptor.getAllValues()).allMatch(projectTable -> projectTable.getId().equals(PROJECT_ID));

		verify(processConnectionDao, times(2)).persist(any(ProcessConnectionTable.class));
	}

	@Test
	public void testSaveProcessModel_ConnectIntermediateThrowEvent() {
		ProjectTable project = new ProjectTable();
		project.setId(PROJECT_ID);

		doNothing().when(processModelDao).persist(any(ProcessModelTable.class));

		ProcessModel processModel = new ProcessModel();
		ProcessEvent event = new ProcessEvent();
		event.setElementId(PROCESS_EVENT_ID);
		event.setEventType(EventType.INTERMEDIATE_THROW);
		event.setLabel(PROCESS_EVENT_LABEL);
		processModel.setEvents(Set.of(event));

		ProcessEventTable eventToConnect1 = new ProcessEventTable();
		eventToConnect1.setEventType(EventType.INTERMEDIATE_CATCH);
		eventToConnect1.setLabel(PROCESS_EVENT_LABEL);

		ProcessEventTable eventToConnect2 = new ProcessEventTable();
		eventToConnect2.setEventType(EventType.START);
		eventToConnect2.setLabel(PROCESS_EVENT_LABEL);

		when(processEventDao.getEvents(project)).thenReturn(List.of(eventToConnect1, eventToConnect2));

		doNothing().when(processConnectionDao).persist(any(ProcessConnectionTable.class));

		repository.saveProcessModel(PROJECT_ID, processModel);

		verify(processModelDao, times(1)).persist(any(ProcessModelTable.class));

		ArgumentCaptor<ProjectTable> projectCaptor = ArgumentCaptor.forClass(ProjectTable.class);
		verify(processEventDao, times(1)).getEvents(project);
		assertThat(projectCaptor.getAllValues()).allMatch(
				capturedProject -> capturedProject.getId().equals(PROJECT_ID));

		verify(processConnectionDao, times(2)).persist(any(ProcessConnectionTable.class));
	}

	@Test
	public void testSaveProcessModel_NoConnectionIfEventLabelsAreNotSimilar() {
		// Arrange
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		ProcessModel processModel = new ProcessModel();
		ProcessEvent event = new ProcessEvent();
		event.setElementId(PROCESS_EVENT_ID);
		event.setEventType(EventType.INTERMEDIATE_THROW);
		event.setLabel(PROCESS_EVENT_LABEL);
		processModel.setEvents(Set.of(event));

		ProcessEventTable eventToConnect = new ProcessEventTable();
		eventToConnect.setEventType(EventType.INTERMEDIATE_CATCH);
		eventToConnect.setLabel(DIFFERENT_EVENT_LABEL);

		doNothing().when(processModelDao).persist(any(ProcessModelTable.class));
		when(processEventDao.getEvents(projectTable)).thenReturn(List.of(eventToConnect));

		when(processModelDao.getProcessModels(projectTable)).thenReturn(List.of());
		when(callActivityDao.getCallActivities(projectTable)).thenReturn(List.of());

		when(dataStoreDao.getDataStores(projectTable)).thenReturn(List.of());

		// Act
		repository.saveProcessModel(PROJECT_ID, processModel);

		// Assert
		verifyNoInteractions(processConnectionDao);

		ArgumentCaptor<ProcessModelTable> processModelTableCaptor = ArgumentCaptor.forClass(ProcessModelTable.class);
		verify(processModelDao, times(1)).persist(processModelTableCaptor.capture());

		verify(processEventDao, times(1)).getEvents(projectTable);
		verify(processModelDao, times(1)).getProcessModels(projectTable);
		verify(callActivityDao, times(1)).getCallActivities(projectTable);
		verify(dataStoreDao, times(1)).getDataStores(projectTable);

		verifyNoMoreInteractions(processModelDao);
		verifyNoMoreInteractions(processEventDao);
		verifyNoMoreInteractions(callActivityDao);
		verifyNoMoreInteractions(dataStoreDao);
	}

	@Test
	public void testSaveProcessModel_CreateDataStoreIfLabelsAreNotSimilar() {
		// Arrange
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		ProcessModel processModel = new ProcessModel();
		ProcessDataStore processDataStore = new ProcessDataStore();
		processDataStore.setLabel(DATA_STORE_LABEL);
		processModel.setDataStores(List.of(processDataStore));

		DataStoreTable dataStoreTable = new DataStoreTable();
		dataStoreTable.setLabel(DIFFERENT_DATA_STORE_LABEL);

		doNothing().when(processModelDao).persist(any(ProcessModelTable.class));
		when(processEventDao.getEvents(projectTable)).thenReturn(List.of());

		when(processModelDao.getProcessModels(projectTable)).thenReturn(List.of());
		when(callActivityDao.getCallActivities(projectTable)).thenReturn(List.of());

		when(dataStoreDao.getDataStores(projectTable)).thenReturn(List.of(dataStoreTable));

		// Act
		repository.saveProcessModel(PROJECT_ID, processModel);

		// Assert
		verifyNoInteractions(processConnectionDao);

		ArgumentCaptor<ProcessModelTable> processModelTableCaptor = ArgumentCaptor.forClass(ProcessModelTable.class);
		verify(processModelDao, times(1)).persist(processModelTableCaptor.capture());

		verify(processEventDao, times(1)).getEvents(projectTable);
		verify(processModelDao, times(1)).getProcessModels(projectTable);
		verify(callActivityDao, times(1)).getCallActivities(projectTable);
		verify(dataStoreDao, times(1)).getDataStores(projectTable);

		ArgumentCaptor<DataStoreTable> dataStoreTableArgumentCaptor = ArgumentCaptor.forClass(DataStoreTable.class);
		verify(dataStoreDao, times(1)).persist(dataStoreTableArgumentCaptor.capture());

		ArgumentCaptor<DataStoreConnectionTable> dataStoreConnectionTableArgumentCaptor = ArgumentCaptor.forClass(
				DataStoreConnectionTable.class);
		verify(dataStoreConnectionDao, times(1)).persist(dataStoreConnectionTableArgumentCaptor.capture());

		verifyNoMoreInteractions(processModelDao);
		verifyNoMoreInteractions(processEventDao);
		verifyNoMoreInteractions(callActivityDao);
		verifyNoMoreInteractions(dataStoreDao);
		verifyNoMoreInteractions(dataStoreConnectionDao);
	}
}
