package de.envite.proa.repository;

import de.envite.proa.entities.datastore.DataAccess;
import de.envite.proa.entities.process.EventType;
import de.envite.proa.entities.process.ProcessConnection;
import de.envite.proa.entities.process.ProcessElementType;
import de.envite.proa.entities.process.ProcessType;
import de.envite.proa.entities.processmap.ProcessMap;
import de.envite.proa.repository.datastore.DataStoreConnectionDao;
import de.envite.proa.repository.datastore.DataStoreDao;
import de.envite.proa.repository.messageflow.MessageFlowDao;
import de.envite.proa.repository.processmap.ProcessMapRepositoryImpl;
import de.envite.proa.repository.processmodel.CallActivityDao;
import de.envite.proa.repository.processmodel.ProcessConnectionDao;
import de.envite.proa.repository.processmodel.ProcessEventDao;
import de.envite.proa.repository.processmodel.ProcessModelDao;
import de.envite.proa.repository.project.ProjectDao;
import de.envite.proa.repository.tables.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ProcessMapRepositoryTest {

	private static final Long PROJECT_ID = 1L;

	private static final Long PROCESS_MODEL_ID_1 = 1L;
	private static final String PROCESS_MODEL_NAME_1 = "processModelName_1";
	private static final String PROCESS_DESCRIPTION_1 = "processDescription_1";

	private static final Long PROCESS_MODEL_ID_2 = 2L;
	private static final String PROCESS_MODEL_NAME_2 = "processModelName_2";
	private static final String PROCESS_DESCRIPTION_2 = "processDescription_2";

	private static final Long PROCESS_MODEL_ID_3 = 3L;
	private static final Long PROCESS_MODEL_ID_4 = 4L;

	private static final String CALLING_ELEMENT_ID = "callingElementId";
	private static final String CALLED_ELEMENT_ID = "calledElementId";

	private static final String COMMON_LABEL = "event label";

	private static final String DATA_STORE_LABEL = "dataStoreLabel";
	private static final Long DATA_STORE_ID = 3L;

	private static final Long MESSAGE_FLOW_ID_2 = 2L;
	private static final List<Long> MESSAGE_FLOW_IDS = List.of(1L, MESSAGE_FLOW_ID_2, 3L);
	private static final String OLD_PARENT_NAME_1 = "oldParentName1";
	private static final String OLD_PARENT_NAME_2 = "oldParentName2";
	private static final String OLD_CHILD_NAME_1 = "oldChildName1";
	private static final String OLD_CHILD_NAME_2 = "oldChildName2";

	private static final Long CONNECTION_ID = 1L;
	private static final Long NEW_CONNECTION_OUTGOING_ID = 2L;
	private static final Long NEW_CONNECTION_INCOMING_ID = 3L;
	private static final Long OLD_CONNECTION_ID_1 = 4L;
	private static final Long OLD_CONNECTION_ID_2 = 5L;
	private static final Long OLD_CONNECTION_ID_3 = 6L;
	private static final Long OLD_CONNECTION_ID_4 = 7L;
	private static final Long OLD_CONNECTION_ID_5 = 8L;
	private static final Long OLD_CONNECTION_ID_6 = 9L;
	private static final Long OLD_CONNECTION_ID_7 = 10L;
	private static final String EVENT_ELEMENT_ID = "eventElementId";
	private static final String CALL_ACTIVITY_ELEMENT_ID = "callActivityElementId";

	private static final ProcessType PROCESS_TYPE_COLLABORATION = ProcessType.COLLABORATION;

	@InjectMocks
	private ProcessMapRepositoryImpl repository;
	@Mock
	private ProjectDao projectDao;
	@Mock
	private ProcessModelDao processModelDao;
	@Mock
	private ProcessConnectionDao processConnectionDao;
	@Mock
	private DataStoreDao dataStoreDao;
	@Mock
	private DataStoreConnectionDao dataStoreConnectionDao;
	@Mock
	private CallActivityDao callActivityDao;
	@Mock
	private ProcessEventDao processEventDao;
	@Mock
	private MessageFlowDao messageFlowDao;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGetProcessMap() {
		// Arrange

		ProcessMapRepositoryImpl repository = new ProcessMapRepositoryImpl(//
				projectDao,
				processModelDao, //
				processConnectionDao, //
				dataStoreDao, //
				dataStoreConnectionDao, //
				callActivityDao, //
				processEventDao, //
				messageFlowDao);

		ProcessModelTable processModel1 = new ProcessModelTable();
		processModel1.setId(PROCESS_MODEL_ID_1);
		processModel1.setName(PROCESS_MODEL_NAME_1);
		processModel1.setDescription(PROCESS_DESCRIPTION_1);

		ProcessModelTable processModel2 = new ProcessModelTable();
		processModel2.setId(PROCESS_MODEL_ID_2);
		processModel2.setName(PROCESS_MODEL_NAME_2);
		processModel2.setDescription(PROCESS_DESCRIPTION_2);

		ProcessModelTable processModelCollaboration = new ProcessModelTable();
		processModelCollaboration.setId(PROCESS_MODEL_ID_3);
		processModelCollaboration.setProcessType(PROCESS_TYPE_COLLABORATION);

		when(processModelDao.getProcessModels(any())).thenReturn(
				Arrays.asList(processModel1, processModel2, processModelCollaboration));

		ProcessConnectionTable processConnectionTable = new ProcessConnectionTable();
		processConnectionTable.setCallingProcess(processModel1);
		processConnectionTable.setCallingElement(CALLING_ELEMENT_ID);
		processConnectionTable.setCallingElementType(ProcessElementType.END_EVENT);
		processConnectionTable.setCalledProcess(processModel2);
		processConnectionTable.setCalledElement(CALLED_ELEMENT_ID);
		processConnectionTable.setCalledElementType(ProcessElementType.START_EVENT);
		processConnectionTable.setUserCreated(false);

		ProcessConnectionTable processConnectionFromCollaboration = new ProcessConnectionTable();
		processConnectionFromCollaboration.setCallingProcess(processModelCollaboration);
		processConnectionFromCollaboration.setCallingElement(CALLING_ELEMENT_ID);
		processConnectionFromCollaboration.setCallingElementType(ProcessElementType.END_EVENT);
		processConnectionFromCollaboration.setCalledProcess(processModelCollaboration);
		processConnectionFromCollaboration.setCalledElement(CALLED_ELEMENT_ID);
		processConnectionFromCollaboration.setCalledElementType(ProcessElementType.START_EVENT);
		processConnectionFromCollaboration.setUserCreated(false);

		ProcessConnectionTable processConnectionToCollaboration = new ProcessConnectionTable();
		processConnectionToCollaboration.setCallingProcess(processModel2);
		processConnectionToCollaboration.setCallingElement(CALLING_ELEMENT_ID);
		processConnectionToCollaboration.setCallingElementType(ProcessElementType.END_EVENT);
		processConnectionToCollaboration.setCalledProcess(processModelCollaboration);
		processConnectionToCollaboration.setCalledElement(CALLED_ELEMENT_ID);
		processConnectionToCollaboration.setCalledElementType(ProcessElementType.START_EVENT);
		processConnectionToCollaboration.setUserCreated(false);

		when(processConnectionDao.getProcessConnections(any())).thenReturn(List.of(
				processConnectionTable, processConnectionFromCollaboration, processConnectionToCollaboration));
		processConnectionTable.setLabel(COMMON_LABEL);

		DataStoreTable dataStoreTable = new DataStoreTable();
		dataStoreTable.setLabel(DATA_STORE_LABEL);
		dataStoreTable.setId(DATA_STORE_ID);
		when(dataStoreDao.getDataStores(any())).thenReturn(List.of(dataStoreTable));

		DataStoreConnectionTable dataStoreConnectionTable = new DataStoreConnectionTable();
		dataStoreConnectionTable.setProcess(processModel1);
		dataStoreConnectionTable.setDataStore(dataStoreTable);
		dataStoreConnectionTable.setAccess(DataAccess.READ_WRITE);

		DataStoreConnectionTable dataStoreConnectionCollaboration = new DataStoreConnectionTable();
		dataStoreConnectionCollaboration.setProcess(processModelCollaboration);
		dataStoreConnectionCollaboration.setDataStore(dataStoreTable);
		dataStoreConnectionCollaboration.setAccess(DataAccess.READ_WRITE);

		when(dataStoreConnectionDao.getDataStoreConnections(any())).thenReturn(
				List.of(dataStoreConnectionTable, dataStoreConnectionCollaboration));

		// Act
		ProcessMap processMap = repository.getProcessMap(anyLong());

		// Assert
		assertThat(processMap.getProcesses())//
				.hasSize(2)//
				.extracting("id", "name", "description")//
				.contains(//
						tuple(PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, PROCESS_DESCRIPTION_1), //
						tuple(PROCESS_MODEL_ID_2, PROCESS_MODEL_NAME_2, PROCESS_DESCRIPTION_2));

		assertThat(processMap.getConnections())//
				.hasSize(1)//
				.extracting("callingProcessid", "callingElementType", "calledProcessid", "calledElementType", "label")//
				.contains(tuple(PROCESS_MODEL_ID_1, ProcessElementType.END_EVENT, PROCESS_MODEL_ID_2,
						ProcessElementType.START_EVENT, COMMON_LABEL));

		assertThat(processMap.getDataStores())//
				.hasSize(1)//
				.extracting("id", "name")//
				.contains(tuple(DATA_STORE_ID, DATA_STORE_LABEL));

		assertThat(processMap.getDataStoreConnections())//
				.hasSize(1)//
				.extracting("processid", "dataStoreId", "access")//
				.contains(tuple(PROCESS_MODEL_ID_1, DATA_STORE_ID, DataAccess.READ_WRITE));
	}

	@Test
	public void testCopyMessageFlowsAndRelations() {
		ProcessModelTable oldProcess = new ProcessModelTable();
		oldProcess.setId(PROCESS_MODEL_ID_1);
		when(processModelDao.findWithParentsAndChildren(PROCESS_MODEL_ID_1)).thenReturn(oldProcess);

		ProcessModelTable newProcess = new ProcessModelTable();
		newProcess.setId(PROCESS_MODEL_ID_2);
		when(processModelDao.findWithParentsAndChildren(PROCESS_MODEL_ID_2)).thenReturn(newProcess);

		ProcessModelTable thirdProcess = new ProcessModelTable();
		thirdProcess.setId(PROCESS_MODEL_ID_3);

		List<MessageFlowTable> messageFlows = new ArrayList<>();
		for (Long messageFlowId : MESSAGE_FLOW_IDS) {
			MessageFlowTable messageFlow = new MessageFlowTable();
			messageFlow.setId(messageFlowId);
			if (Objects.equals(messageFlowId, MESSAGE_FLOW_ID_2)) {
				messageFlow.setCallingProcess(oldProcess);
				messageFlow.setCalledProcess(thirdProcess);
			} else {
				messageFlow.setCalledProcess(oldProcess);
				messageFlow.setCallingProcess(thirdProcess);
			}
			doNothing().when(messageFlowDao).merge(messageFlow);
			messageFlows.add(messageFlow);
		}

		when(messageFlowDao.getMessageFlows(any(), eq(oldProcess))).thenReturn(messageFlows);

		ProcessModelTable oldParent1 = new ProcessModelTable();
		oldParent1.setName(OLD_PARENT_NAME_1);
		ProcessModelTable oldParent2 = new ProcessModelTable();
		oldParent2.setName(OLD_PARENT_NAME_2);
		List<ProcessModelTable> oldParents = List.of(oldParent1, oldParent2);
		oldProcess.getParents().addAll(oldParents);
		newProcess.getParents().add(oldParent1);

		doNothing().when(processModelDao).merge(oldParent1);
		doNothing().when(processModelDao).merge(oldParent2);

		ProcessModelTable oldChild1 = new ProcessModelTable();
		oldChild1.setName(OLD_CHILD_NAME_1);
		ProcessModelTable oldChild2 = new ProcessModelTable();
		oldChild2.setName(OLD_CHILD_NAME_2);
		List<ProcessModelTable> oldChildren = List.of(oldChild1, oldChild2);
		oldProcess.getChildren().addAll(oldChildren);
		newProcess.getChildren().add(oldChild1);

		doNothing().when(processModelDao).merge(oldChild1);
		doNothing().when(processModelDao).merge(oldChild2);

		doNothing().when(processModelDao).merge(newProcess);
		doNothing().when(processModelDao).merge(oldProcess);

		repository.copyMessageFlowsAndRelations(PROJECT_ID, PROCESS_MODEL_ID_1, PROCESS_MODEL_ID_2);

		verify(processModelDao, times(1)).findWithParentsAndChildren(PROCESS_MODEL_ID_1);
		verify(processModelDao, times(1)).findWithParentsAndChildren(PROCESS_MODEL_ID_2);
		
		
		ArgumentCaptor<ProjectTable> projectCaptor = ArgumentCaptor.forClass(ProjectTable.class);
		verify(messageFlowDao, times(1)).getMessageFlows(projectCaptor.capture(), eq(oldProcess));
		assertThat(projectCaptor.getValue().getId()).isEqualTo(PROJECT_ID);
		messageFlows.forEach(messageFlow -> verify(messageFlowDao, times(1)).merge(messageFlow));
		verify(processModelDao, times(1)).merge(oldParents.get(0));
		verify(processModelDao, times(1)).merge(oldParents.get(1));
		verify(processModelDao, times(1)).merge(oldChildren.get(0));
		verify(processModelDao, times(1)).merge(oldChildren.get(1));
		verify(processModelDao, times(1)).merge(newProcess);
		verify(processModelDao, times(1)).merge(oldProcess);
	}

	@Test
	public void testDeleteProcessConnection() {
		doNothing().when(processConnectionDao).deleteConnection(CONNECTION_ID);

		repository.deleteProcessConnection(CONNECTION_ID);

		verify(processConnectionDao, times(1)).deleteConnection(CONNECTION_ID);
	}

	@Test
	public void testDeleteDataStoreConnection() {
		doNothing().when(dataStoreConnectionDao).deleteConnection(CONNECTION_ID);

		repository.deleteDataStoreConnection(CONNECTION_ID);

		verify(dataStoreConnectionDao, times(1)).deleteConnection(CONNECTION_ID);
	}

	@Test
	public void testAddConnection() {
		ProcessConnection connection = new ProcessConnection();
		connection.setCalledElementType(ProcessElementType.START_EVENT);
		connection.setCallingElementType(ProcessElementType.END_EVENT);

		doNothing().when(processConnectionDao).addConnection(any(ProcessConnectionTable.class));

		repository.addConnection(PROJECT_ID, connection);

		verify(processConnectionDao, times(1)).addConnection(any(ProcessConnectionTable.class));
	}

	@Test
	public void testCopyConnections() {
		ProjectTable project = new ProjectTable();
		when(projectDao.findById(PROJECT_ID)).thenReturn(project);

		ProcessModelTable oldProcess = new ProcessModelTable();
		oldProcess.setId(PROCESS_MODEL_ID_1);
		when(processModelDao.find(PROCESS_MODEL_ID_1)).thenReturn(oldProcess);

		ProcessModelTable newProcess = new ProcessModelTable();
		newProcess.setId(PROCESS_MODEL_ID_2);
		when(processModelDao.find(PROCESS_MODEL_ID_2)).thenReturn(newProcess);

		ProcessModelTable otherProcess = new ProcessModelTable();
		otherProcess.setId(PROCESS_MODEL_ID_3);

		ProcessModelTable thirdProcess = new ProcessModelTable();
		thirdProcess.setId(PROCESS_MODEL_ID_4);
		when(processModelDao.find(PROCESS_MODEL_ID_4)).thenReturn(thirdProcess);

		ProcessConnectionTable newConnectionOutgoing = new ProcessConnectionTable();
		newConnectionOutgoing.setId(NEW_CONNECTION_OUTGOING_ID);
		newConnectionOutgoing.setCallingProcess(newProcess);
		newConnectionOutgoing.setCalledProcess(otherProcess);

		ProcessConnectionTable newConnectionIncoming = new ProcessConnectionTable();
		newConnectionIncoming.setId(NEW_CONNECTION_INCOMING_ID);
		newConnectionIncoming.setCalledProcess(newProcess);
		newConnectionIncoming.setCallingProcess(otherProcess);

		ProcessConnectionTable oldConnection_Outgoing_ToNewConnectionTargets = new ProcessConnectionTable();
		oldConnection_Outgoing_ToNewConnectionTargets.setId(OLD_CONNECTION_ID_1);
		oldConnection_Outgoing_ToNewConnectionTargets.setCallingProcess(oldProcess);
		oldConnection_Outgoing_ToNewConnectionTargets.setCalledProcess(otherProcess);
		oldConnection_Outgoing_ToNewConnectionTargets.setUserCreated(true);

		ProcessConnectionTable oldConnection_Outgoing_ToNewProcess = new ProcessConnectionTable();
		oldConnection_Outgoing_ToNewProcess.setId(OLD_CONNECTION_ID_2);
		oldConnection_Outgoing_ToNewProcess.setCallingProcess(oldProcess);
		oldConnection_Outgoing_ToNewProcess.setCalledProcess(newProcess);
		oldConnection_Outgoing_ToNewProcess.setUserCreated(true);

		ProcessConnectionTable oldConnection_Incoming_FromNewConnectionSources = new ProcessConnectionTable();
		oldConnection_Incoming_FromNewConnectionSources.setId(OLD_CONNECTION_ID_3);
		oldConnection_Incoming_FromNewConnectionSources.setCalledProcess(oldProcess);
		oldConnection_Incoming_FromNewConnectionSources.setCallingProcess(otherProcess);
		oldConnection_Incoming_FromNewConnectionSources.setUserCreated(true);

		ProcessConnectionTable oldConnection_Incoming_FromNewProcess = new ProcessConnectionTable();
		oldConnection_Incoming_FromNewProcess.setId(OLD_CONNECTION_ID_4);
		oldConnection_Incoming_FromNewProcess.setCalledProcess(oldProcess);
		oldConnection_Incoming_FromNewProcess.setCallingProcess(newProcess);
		oldConnection_Incoming_FromNewProcess.setUserCreated(true);

		ProcessConnectionTable oldConnection_Outgoing_Valid = new ProcessConnectionTable();
		oldConnection_Outgoing_Valid.setId(OLD_CONNECTION_ID_5);
		oldConnection_Outgoing_Valid.setCallingProcess(oldProcess);
		oldConnection_Outgoing_Valid.setCalledProcess(thirdProcess);
		oldConnection_Outgoing_Valid.setCallingElementType(ProcessElementType.INTERMEDIATE_THROW_EVENT);
		oldConnection_Outgoing_Valid.setCalledElementType(ProcessElementType.INTERMEDIATE_CATCH_EVENT);
		oldConnection_Outgoing_Valid.setUserCreated(true);

		ProcessConnectionTable oldConnection_Incoming_Valid = new ProcessConnectionTable();
		oldConnection_Incoming_Valid.setId(OLD_CONNECTION_ID_6);
		oldConnection_Incoming_Valid.setCalledProcess(oldProcess);
		oldConnection_Incoming_Valid.setCallingProcess(thirdProcess);
		oldConnection_Incoming_Valid.setCallingElementType(ProcessElementType.CALL_ACTIVITY);
		oldConnection_Incoming_Valid.setCalledElementType(ProcessElementType.INVALID);
		oldConnection_Incoming_Valid.setUserCreated(true);

		ProcessConnectionTable oldConnection_Outgoing_Valid_ExistingCallActivity = new ProcessConnectionTable();
		oldConnection_Outgoing_Valid_ExistingCallActivity.setId(OLD_CONNECTION_ID_7);
		oldConnection_Outgoing_Valid_ExistingCallActivity.setCalledProcess(thirdProcess);
		oldConnection_Outgoing_Valid_ExistingCallActivity.setCallingProcess(oldProcess);
		oldConnection_Outgoing_Valid_ExistingCallActivity.setCallingElementType(ProcessElementType.CALL_ACTIVITY);
		oldConnection_Outgoing_Valid_ExistingCallActivity.setCalledElementType(ProcessElementType.START_EVENT);
		oldConnection_Outgoing_Valid_ExistingCallActivity.setUserCreated(true);

		List<ProcessConnectionTable> oldConnections = List.of(oldConnection_Outgoing_ToNewConnectionTargets,
				oldConnection_Outgoing_ToNewProcess, oldConnection_Incoming_FromNewConnectionSources,
				oldConnection_Incoming_FromNewProcess, oldConnection_Outgoing_Valid,
				oldConnection_Incoming_Valid, oldConnection_Outgoing_Valid_ExistingCallActivity);

		ProcessEventTable event = new ProcessEventTable();
		event.setElementId(EVENT_ELEMENT_ID);

		CallActivityTable callActivity = new CallActivityTable();
		callActivity.setElementId(CALL_ACTIVITY_ELEMENT_ID);

		when(callActivityDao.findForProcessModel(thirdProcess)).thenReturn(null);
		when(callActivityDao.findForProcessModel(newProcess)).thenReturn(callActivity);

		when(processEventDao.findForProcessModelAndEventType(newProcess, null)).thenReturn(null);
		when(processEventDao.findForProcessModelAndEventType(newProcess, EventType.INTERMEDIATE_THROW)).thenReturn(
				null);
		when(processEventDao.findForProcessModelAndEventType(thirdProcess, EventType.INTERMEDIATE_CATCH)).thenReturn(
				event);
		when(processEventDao.findForProcessModelAndEventType(thirdProcess, EventType.START)).thenReturn(null);

		when(processConnectionDao.getProcessConnections(project, oldProcess)).thenReturn(oldConnections);
		when(processConnectionDao.getProcessConnections(project, newProcess)).thenReturn(
				List.of(newConnectionOutgoing, newConnectionIncoming));
		doNothing().when(processConnectionDao).persist(any(ProcessConnectionTable.class));

		repository.copyConnections(PROJECT_ID, PROCESS_MODEL_ID_1, PROCESS_MODEL_ID_2);

		verify(projectDao, times(4)).findById(any());
		verify(projectDao, times(4)).findById(PROJECT_ID);

		verify(callActivityDao, times(2)).findForProcessModel(any());
		verify(callActivityDao, times(1)).findForProcessModel(thirdProcess);
		verify(callActivityDao, times(1)).findForProcessModel(newProcess);

		verify(processModelDao, times(8)).find(any());
		verify(processModelDao, times(1)).find(PROCESS_MODEL_ID_1);
		verify(processModelDao, times(4)).find(PROCESS_MODEL_ID_2);
		verify(processModelDao, times(3)).find(PROCESS_MODEL_ID_4);

		verify(processEventDao, times(4)).findForProcessModelAndEventType(any(), any());
		verify(processEventDao, times(1)).findForProcessModelAndEventType(newProcess, null);
		verify(processEventDao, times(1)).findForProcessModelAndEventType(newProcess, EventType.INTERMEDIATE_THROW);
		verify(processEventDao, times(1)).findForProcessModelAndEventType(thirdProcess, EventType.INTERMEDIATE_CATCH);
		verify(processEventDao, times(1)).findForProcessModelAndEventType(thirdProcess, EventType.START);

		verify(processConnectionDao, times(2)).getProcessConnections(any(), any());
		verify(processConnectionDao, times(1)).getProcessConnections(project, oldProcess);
		verify(processConnectionDao, times(1)).getProcessConnections(project, newProcess);

		verify(processConnectionDao, times(3)).persist(any());
		verify(processConnectionDao, times(3)).persist(any(ProcessConnectionTable.class));
	}
}