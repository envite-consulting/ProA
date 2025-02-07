package de.envite.proa.repository;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.datastore.DataAccess;
import de.envite.proa.entities.process.*;
import de.envite.proa.repository.datastore.DataStoreConnectionDao;
import de.envite.proa.repository.datastore.DataStoreDao;
import de.envite.proa.repository.messageflow.MessageFlowDao;
import de.envite.proa.repository.processmodel.*;
import de.envite.proa.repository.project.ProjectDao;
import de.envite.proa.repository.tables.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ProcessmodelRepositoryTest {

	private static final String PROCESS_DESCRIPTION = "Description";
	private static final String COMMON_EVENT_LABEL = "common event label";
	private static final String EXISTING_PROCESS_MODEL_NAME = "TestProcessModel";
	private static final String NEW_PROCESS_MODEL_NAME = "NewTestProcessModel";
	private static final String EXISTING_EVENT_ID = "existingEventID";
	private static final String NEW_EVENT_ID = "newEventID";
	private static final String NEW_ACTIVITY_ID = "newActivityId";
	private static final String EXISTING_ACTIVITY_ID = "existingActivityId";
	private static final String DATA_STORE_LABEL = "DataStoreLabel";
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

	@InjectMocks
	private ProcessmodelRepositoryImpl repository;
	@Mock
	private ProjectDao projectDao;
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
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testEndEvent() {

		// Arrange
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		when(projectDao.findById(PROJECT_ID)).thenReturn(projectTable);

		ProcessEventTable processEventTable = new ProcessEventTable();
		processEventTable.setEventType(EventType.START);
		processEventTable.setLabel(COMMON_EVENT_LABEL);
		processEventTable.setElementId(EXISTING_EVENT_ID);
		ProcessModelTable processModelTable = new ProcessModelTable();
		processModelTable.setName(EXISTING_PROCESS_MODEL_NAME);
		processEventTable.setProcessModel(processModelTable);
		List<ProcessEventTable> startEventTables = List.of(processEventTable);
		when(processEventDao.getEventsForLabelAndType(COMMON_EVENT_LABEL, EventType.START, projectTable))
				.thenReturn(startEventTables);

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				projectDao, //
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

		model.setEvents(List.of(endEvent));

		// Act
		repository.saveProcessModel(PROJECT_ID, model);

		// Assert
		ArgumentCaptor<ProcessConnectionTable> connectionCaptor = ArgumentCaptor.forClass(ProcessConnectionTable.class);
		verify(processConnectionDao).persist(connectionCaptor.capture());
		ProcessConnectionTable connection = connectionCaptor.getValue();

		assertThat(connection.getCalledProcess().getName()).isEqualTo(EXISTING_PROCESS_MODEL_NAME);
		assertThat(connection.getCalledElement()).isEqualTo(EXISTING_EVENT_ID);
		assertThat(connection.getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);

		assertThat(connection.getCallingProcess().getName()).isEqualTo(NEW_PROCESS_MODEL_NAME);
		assertThat(connection.getCallingElement()).isEqualTo(NEW_EVENT_ID);
		assertThat(connection.getCallingElementType()).isEqualTo(ProcessElementType.END_EVENT);

		assertThat(connection.getLabel()).isEqualTo(COMMON_EVENT_LABEL);

		assertThat(connection.getUserCreated()).isEqualTo(false);
	}

	@Test
	public void testStartEvent() {

		// Arrange
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		when(projectDao.findById(PROJECT_ID)).thenReturn(projectTable);

		ProcessEventTable processEventTable = new ProcessEventTable();
		processEventTable.setEventType(EventType.END);
		processEventTable.setLabel(COMMON_EVENT_LABEL);
		processEventTable.setElementId(EXISTING_EVENT_ID);
		ProcessModelTable processModelTable = new ProcessModelTable();
		processModelTable.setName(EXISTING_PROCESS_MODEL_NAME);
		processEventTable.setProcessModel(processModelTable);
		List<ProcessEventTable> endEventTables = Arrays.asList(processEventTable);
		when(processEventDao.getEventsForLabelAndType(COMMON_EVENT_LABEL, EventType.END, projectTable))
				.thenReturn(endEventTables);

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				projectDao, //
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

		model.setEvents(Arrays.asList(startEvent));

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

		assertThat(connection.getUserCreated()).isEqualTo(false);
	}

	@Test
	public void testCallActivity() {

		// Arrange
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		when(projectDao.findById(PROJECT_ID)).thenReturn(projectTable);

		ProcessModelTable processModelTable = new ProcessModelTable();
		processModelTable.setName(EXISTING_PROCESS_MODEL_NAME);
		when(processModelDao.getProcessModelsForName(EXISTING_PROCESS_MODEL_NAME, projectTable))
				.thenReturn(Arrays.asList(processModelTable));

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				projectDao, //
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

		model.setCallActivities(Arrays.asList(activity));

		// Act
		repository.saveProcessModel(PROJECT_ID, model);

		// Assert
		ArgumentCaptor<ProcessConnectionTable> connectionCaptor = ArgumentCaptor.forClass(ProcessConnectionTable.class);
		verify(processConnectionDao).persist(connectionCaptor.capture());
		ProcessConnectionTable connection = connectionCaptor.getValue();

		assertThat(connection.getCallingProcess().getName()).isEqualTo(NEW_PROCESS_MODEL_NAME);
		assertThat(connection.getCallingElement()).isEqualTo(NEW_ACTIVITY_ID);
		assertThat(connection.getCallingElementType()).isEqualTo(ProcessElementType.CALL_ACTIVITY);

		assertThat(connection.getCalledProcess().getName()).isEqualTo(EXISTING_PROCESS_MODEL_NAME);
		assertThat(connection.getCalledElement()).isNull();
		assertThat(connection.getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);

		assertThat(connection.getLabel()).isEqualTo(EXISTING_PROCESS_MODEL_NAME);

		assertThat(connection.getUserCreated()).isEqualTo(false);
	}

	@Test
	public void testProcessCalledByActivity() {

		// Arrange
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		when(projectDao.findById(PROJECT_ID)).thenReturn(projectTable);

		ProcessModelTable processModelTable = new ProcessModelTable();
		processModelTable.setName(EXISTING_PROCESS_MODEL_NAME);

		CallActivityTable callActivityTable = new CallActivityTable();
		callActivityTable.setElementId(EXISTING_ACTIVITY_ID);
		callActivityTable.setLabel(NEW_PROCESS_MODEL_NAME);
		callActivityTable.setProcessModel(processModelTable);

		when(callActivityDao.getCallActivitiesForName(NEW_PROCESS_MODEL_NAME, projectTable))
				.thenReturn(Arrays.asList(callActivityTable));

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				projectDao, //
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
		ProcessConnectionTable connection = connectionCaptor.getValue();

		assertThat(connection.getCallingProcess().getName()).isEqualTo(EXISTING_PROCESS_MODEL_NAME);
		assertThat(connection.getCallingElement()).isEqualTo(EXISTING_ACTIVITY_ID);
		assertThat(connection.getCallingElementType()).isEqualTo(ProcessElementType.CALL_ACTIVITY);

		assertThat(connection.getCalledProcess().getName()).isEqualTo(NEW_PROCESS_MODEL_NAME);
		assertThat(connection.getCalledElement()).isNull();
		assertThat(connection.getCalledElementType()).isEqualTo(ProcessElementType.START_EVENT);

		assertThat(connection.getLabel()).isEqualTo(NEW_PROCESS_MODEL_NAME);

		assertThat(connection.getUserCreated()).isEqualTo(false);
	}

	@Test
	public void testDataStore() {
		// Arrange
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		when(projectDao.findById(PROJECT_ID)).thenReturn(projectTable);

		DataStoreTable dataStoreTable = new DataStoreTable();
		dataStoreTable.setLabel(DATA_STORE_LABEL);

		when(dataStoreDao.getDataStoreForLabel(DATA_STORE_LABEL, projectTable)).thenReturn(dataStoreTable);

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				projectDao, //
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

		model.setDataStores(Arrays.asList(dataStore));

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
	}

	@Test
	public void testGetProcessInformation() {

		// Arrange
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		when(projectDao.findById(PROJECT_ID)).thenReturn(projectTable);

		LocalDateTime dateTime = LocalDateTime.now();

		ProcessModelTable processModel = new ProcessModelTable();
		processModel.setId(PROCESS_MODEL_ID_1);
		processModel.setName(EXISTING_PROCESS_MODEL_NAME);
		processModel.setDescription(PROCESS_DESCRIPTION);
		processModel.setCreatedAt(dateTime);

		when(processModelDao.getProcessModels(projectTable)).thenReturn(Arrays.asList(processModel));

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				projectDao, //
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
	}

	@Test
	public void testFindByNameOrBpmnProcessId_FindById() {
		ProjectTable project = new ProjectTable();
		ProcessModelTable process = new ProcessModelTable();

		when(projectDao.findById(PROJECT_ID)).thenReturn(project);
		when(processModelDao.findByName(NEW_PROCESS_MODEL_NAME, project)).thenReturn(null);
		when(processModelDao.findByBpmnProcessId(BPMN_PROCESS_ID, project)).thenReturn(process);

		ProcessModelTable result = repository.findByNameOrBpmnProcessId(NEW_PROCESS_MODEL_NAME, BPMN_PROCESS_ID,
				PROJECT_ID);

		assertThat(result).isEqualTo(process);
		verify(projectDao, times(1)).findById(PROJECT_ID);
		verify(processModelDao, times(1)).findByName(NEW_PROCESS_MODEL_NAME, project);
		verify(processModelDao, times(1)).findByBpmnProcessId(BPMN_PROCESS_ID, project);
	}

	@Test
	public void testFindByNameOrBpmnProcessId_FindByName() {
		ProjectTable project = new ProjectTable();
		ProcessModelTable process = new ProcessModelTable();

		when(projectDao.findById(PROJECT_ID)).thenReturn(project);
		when(processModelDao.findByName(EXISTING_PROCESS_MODEL_NAME, project)).thenReturn(process);

		ProcessModelTable result = repository.findByNameOrBpmnProcessId(EXISTING_PROCESS_MODEL_NAME, BPMN_PROCESS_ID,
				PROJECT_ID);

		assertThat(result).isEqualTo(process);
		verify(projectDao, times(1)).findById(PROJECT_ID);
		verify(processModelDao, times(1)).findByName(EXISTING_PROCESS_MODEL_NAME, project);
		verify(processModelDao, never()).findByBpmnProcessId(any(), any());
	}

	@Test
	public void testGetProcessModel() {
		ProcessModelTable processModel = new ProcessModelTable();
		processModel.setBpmnXml(BPMN_XML);

		when(processModelDao.find(PROCESS_MODEL_ID_1)).thenReturn(processModel);

		String result = repository.getProcessModel(PROCESS_MODEL_ID_1);

		assertThat(result.getBytes()).isEqualTo(BPMN_XML);
		verify(processModelDao, times(1)).find(PROCESS_MODEL_ID_1);
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
		when(projectDao.findById(PROJECT_ID)).thenReturn(project);

		ProcessModel processModel = new ProcessModel();
		processModel.setName(PROCESS_MODEL_NAME);
		processModel.setParentBpmnProcessId(PARENT_PROCESS_MODEL_ID);

		doNothing().when(processModelDao).persist(any(ProcessModelTable.class));

		ProcessModelTable parent = new ProcessModelTable();
		when(processModelDao.findByBpmnProcessId(PARENT_PROCESS_MODEL_ID, project)).thenReturn(parent);

		doNothing().when(processModelDao).merge(any(ProcessModelTable.class));

		repository.saveProcessModel(PROJECT_ID, processModel);

		verify(projectDao, times(1)).findById(PROJECT_ID);
		verify(processModelDao, times(1)).persist(any(ProcessModelTable.class));
		verify(processModelDao, times(1)).findByBpmnProcessId(PARENT_PROCESS_MODEL_ID, project);
		verify(processModelDao, times(2)).merge(any(ProcessModelTable.class));
	}

	@Test
	public void testDeleteProcessModel_DeleteParentsAndChildren() {
		ProcessModelTable processModel = new ProcessModelTable();
		processModel.setId(PROCESS_MODEL_ID_1);

		ProcessModelTable parent = new ProcessModelTable();
		parent.setId(PROCESS_MODEL_ID_2);
		parent.setChildren(List.of(processModel));
		processModel.setParents(List.of(parent));

		ProcessModelTable child1 = new ProcessModelTable();
		child1.setId(PROCESS_MODEL_ID_3);
		child1.setParents(List.of(processModel));
		ProcessModelTable child2 = new ProcessModelTable();
		child2.setId(PROCESS_MODEL_ID_4);
		ProcessModelTable otherParent = new ProcessModelTable();
		otherParent.setId(PROCESS_MODEL_ID_5);
		child2.setParents(List.of(processModel, otherParent));

		processModel.setChildren(List.of(child1, child2));

		when(processModelDao.find(PROCESS_MODEL_ID_1)).thenReturn(processModel);
		when(processModelDao.find(PROCESS_MODEL_ID_2)).thenReturn(parent);
		when(processModelDao.find(PROCESS_MODEL_ID_3)).thenReturn(child1);
		when(processModelDao.find(PROCESS_MODEL_ID_4)).thenReturn(child2);
		when(processModelDao.find(PROCESS_MODEL_ID_5)).thenReturn(otherParent);

		doNothing().when(dataStoreConnectionDao).deleteForProcessModel(any(Long.class));
		doNothing().when(processConnectionDao).deleteForProcessModel(any(Long.class));
		List<Long> expectedIds = List.of(PROCESS_MODEL_ID_1, PROCESS_MODEL_ID_3, PROCESS_MODEL_ID_2);
		doNothing().when(processModelDao).delete(expectedIds);

		repository.deleteProcessModel(PROCESS_MODEL_ID_1);

		verify(processModelDao, times(1)).find(PROCESS_MODEL_ID_1);
		verify(processModelDao, times(1)).find(PROCESS_MODEL_ID_2);
		verify(processModelDao, times(1)).find(PROCESS_MODEL_ID_3);

		verify(dataStoreConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_1);
		verify(dataStoreConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_3);
		verify(dataStoreConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_2);

		verify(processConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_1);
		verify(processConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_3);
		verify(processConnectionDao, times(1)).deleteForProcessModel(PROCESS_MODEL_ID_2);

		verify(processModelDao, times(1)).delete(expectedIds);
	}

	@Test
	public void testSaveProcessModel_UnknownEventType() {
		ProjectTable project = new ProjectTable();
		ProcessModel processModel = new ProcessModel();
		ProcessEvent event = new ProcessEvent();
		event.setEventType(EventType.INVALID);
		processModel.setEvents(List.of(event));

		when(projectDao.findById(PROJECT_ID)).thenReturn(project);
		doNothing().when(processModelDao).persist(any(ProcessModelTable.class));

		assertThrows(IllegalArgumentException.class, () -> repository.saveProcessModel(PROJECT_ID, processModel));

		verify(projectDao, times(1)).findById(PROJECT_ID);
		verify(processModelDao, times(1)).persist(any(ProcessModelTable.class));
	}

	@Test
	public void testSaveProcessModel_ConnectIntermediateCatchEvent() {
		ProjectTable project = new ProjectTable();
		when(projectDao.findById(PROJECT_ID)).thenReturn(project);

		doNothing().when(processModelDao).persist(any(ProcessModelTable.class));

		ProcessModel processModel = new ProcessModel();
		ProcessEvent event = new ProcessEvent();
		event.setElementId(PROCESS_EVENT_ID);
		event.setEventType(EventType.INTERMEDIATE_CATCH);
		event.setLabel(PROCESS_EVENT_LABEL);
		processModel.setEvents(List.of(event));

		ProcessEventTable eventToConnect = new ProcessEventTable();

		when(processEventDao.getEventsForLabelAndType(PROCESS_EVENT_LABEL, EventType.END, project)).thenReturn(
				List.of(eventToConnect));
		when(processEventDao.getEventsForLabelAndType(PROCESS_EVENT_LABEL, EventType.INTERMEDIATE_THROW,
				project)).thenReturn(List.of(eventToConnect));

		doNothing().when(processConnectionDao).persist(any(ProcessConnectionTable.class));

		repository.saveProcessModel(PROJECT_ID, processModel);

		verify(projectDao, times(1)).findById(PROJECT_ID);
		verify(processModelDao, times(1)).persist(any(ProcessModelTable.class));
		verify(processEventDao, times(1)).getEventsForLabelAndType(PROCESS_EVENT_LABEL, EventType.END, project);
		verify(processEventDao, times(1)).getEventsForLabelAndType(PROCESS_EVENT_LABEL, EventType.INTERMEDIATE_THROW,
				project);
		verify(processConnectionDao, times(2)).persist(any(ProcessConnectionTable.class));
	}

	@Test
	public void testSaveProcessModel_ConnectIntermediateThrowEvent() {
		ProjectTable project = new ProjectTable();
		when(projectDao.findById(PROJECT_ID)).thenReturn(project);

		doNothing().when(processModelDao).persist(any(ProcessModelTable.class));

		ProcessModel processModel = new ProcessModel();
		ProcessEvent event = new ProcessEvent();
		event.setElementId(PROCESS_EVENT_ID);
		event.setEventType(EventType.INTERMEDIATE_THROW);
		event.setLabel(PROCESS_EVENT_LABEL);
		processModel.setEvents(List.of(event));

		ProcessEventTable eventToConnect = new ProcessEventTable();

		when(processEventDao.getEventsForLabelAndType(PROCESS_EVENT_LABEL, EventType.START, project)).thenReturn(
				List.of(eventToConnect));
		when(processEventDao.getEventsForLabelAndType(PROCESS_EVENT_LABEL, EventType.INTERMEDIATE_CATCH,
				project)).thenReturn(List.of(eventToConnect));

		doNothing().when(processConnectionDao).persist(any(ProcessConnectionTable.class));

		repository.saveProcessModel(PROJECT_ID, processModel);

		verify(projectDao, times(1)).findById(PROJECT_ID);
		verify(processModelDao, times(1)).persist(any(ProcessModelTable.class));
		verify(processEventDao, times(1)).getEventsForLabelAndType(PROCESS_EVENT_LABEL, EventType.START, project);
		verify(processEventDao, times(1)).getEventsForLabelAndType(PROCESS_EVENT_LABEL, EventType.INTERMEDIATE_CATCH,
				project);
		verify(processConnectionDao, times(2)).persist(any(ProcessConnectionTable.class));
	}
}