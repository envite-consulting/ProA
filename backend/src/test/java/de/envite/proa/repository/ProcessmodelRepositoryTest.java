package de.envite.proa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.envite.proa.entities.DataAccess;
import de.envite.proa.entities.EventType;
import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessDataStore;
import de.envite.proa.entities.ProcessElementType;
import de.envite.proa.entities.ProcessEvent;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.entities.ProcessModel;
import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.DataStoreConnectionTable;
import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProcessConnectionTable;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;

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
	private static final Long PROCESSM_MODEL_ID = 1L;
	private static final Long PROJECT_ID = 2341234L;

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
		List<ProcessEventTable> startEventTables = Arrays.asList(processEventTable);
		when(processEventDao.getEventsForLabelAndType(COMMON_EVENT_LABEL, EventType.START, projectTable))
				.thenReturn(startEventTables);

		ProcessmodelRepositoryImpl repository = new ProcessmodelRepositoryImpl(//
				projectDao, //
				processModelDao, //
				dataStoreDao, //
				dataStoreConnectionDao, //
				callActivityDao, //
				processConnectionDao, //
				processEventDao);

		ProcessModel model = new ProcessModel();
		model.setName(NEW_PROCESS_MODEL_NAME);

		ProcessEvent endEvent = new ProcessEvent();
		endEvent.setElementId(NEW_EVENT_ID);
		endEvent.setLabel(COMMON_EVENT_LABEL);
		endEvent.setEventType(EventType.END);

		model.setEvents(Arrays.asList(endEvent));

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
				processEventDao);

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
				processEventDao);

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
				processEventDao);

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
				processEventDao);

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
		processModel.setId(PROCESSM_MODEL_ID);
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
				processEventDao);

		// Act
		List<ProcessInformation> processInformation = repository.getProcessInformation(PROJECT_ID);

		// Assert
		assertThat(processInformation)//
				.hasSize(1)//
				.extracting("id", "processName", "description", "createdAt")//
				.contains(tuple(PROCESSM_MODEL_ID, EXISTING_PROCESS_MODEL_NAME, PROCESS_DESCRIPTION, dateTime));
	}
}