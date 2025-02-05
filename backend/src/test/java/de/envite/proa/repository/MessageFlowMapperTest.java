package de.envite.proa.repository;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.process.ProcessElementType;
import de.envite.proa.repository.messageflow.MessageFlowMapper;
import de.envite.proa.repository.processmodel.ProcessModelDao;
import de.envite.proa.repository.tables.MessageFlowTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageFlowMapperTest {

	private static final Long PROJECT_ID = 1L;
	private static final String BPMN_ID = "bpmn_123";
	private static final String NAME = "Test Flow";
	private static final String DESCRIPTION = "Test Description";
	private static final Long CALLING_PROCESS_ID = 1L;
	private static final Long CALLED_PROCESS_ID = 2L;
	private static final ProcessElementType CALLING_ELEMENT_TYPE = ProcessElementType.END_EVENT;
	private static final ProcessElementType CALLED_ELEMENT_TYPE = ProcessElementType.START_EVENT;

	@Mock
	private ProcessModelDao processModelDao;

	private ProjectTable project;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		project = new ProjectTable();
		project.setId(PROJECT_ID);
	}

	@Test
	void testClassInitialization() {
		MessageFlowMapper mapper = new MessageFlowMapper();
		assertNotNull(mapper);
	}

	@Test
	void testMapMessageFlowDetailsToTable() {
		ProcessModelTable callingProcess = new ProcessModelTable();
		callingProcess.setId(CALLING_PROCESS_ID);
		ProcessModelTable calledProcess = new ProcessModelTable();
		calledProcess.setId(CALLED_PROCESS_ID);

		when(processModelDao.find(CALLING_PROCESS_ID)).thenReturn(callingProcess);
		when(processModelDao.find(CALLED_PROCESS_ID)).thenReturn(calledProcess);

		MessageFlowDetails details = new MessageFlowDetails();
		details.setBpmnId(BPMN_ID);
		details.setName(NAME);
		details.setDescription(DESCRIPTION);
		details.setCallingProcessId(CALLING_PROCESS_ID);
		details.setCalledProcessId(CALLED_PROCESS_ID);
		details.setCallingElementType(CALLING_ELEMENT_TYPE);
		details.setCalledElementType(CALLED_ELEMENT_TYPE);

		MessageFlowTable table = MessageFlowMapper.map(details, project, processModelDao);

		assertNotNull(table);
		assertEquals(BPMN_ID, table.getBpmnId());
		assertEquals(NAME, table.getName());
		assertEquals(DESCRIPTION, table.getDescription());
		assertEquals(callingProcess, table.getCallingProcess());
		assertEquals(calledProcess, table.getCalledProcess());
		assertEquals(CALLING_ELEMENT_TYPE, table.getCallingElementType());
		assertEquals(CALLED_ELEMENT_TYPE, table.getCalledElementType());
		assertEquals(project, table.getProject());

		verify(processModelDao).find(CALLING_PROCESS_ID);
		verify(processModelDao).find(CALLED_PROCESS_ID);
	}

	@Test
	void testMapTableToMessageFlowDetails() {
		MessageFlowTable table = getMessageFlowTable();

		MessageFlowDetails details = MessageFlowMapper.map(table);

		assertNotNull(details);
		assertEquals(BPMN_ID, details.getBpmnId());
		assertEquals(NAME, details.getName());
		assertEquals(DESCRIPTION, details.getDescription());
		assertEquals(CALLING_PROCESS_ID, details.getCallingProcessId());
		assertEquals(CALLED_PROCESS_ID, details.getCalledProcessId());
		assertEquals(CALLING_ELEMENT_TYPE, details.getCallingElementType());
		assertEquals(CALLED_ELEMENT_TYPE, details.getCalledElementType());
	}

	private static MessageFlowTable getMessageFlowTable() {
		ProcessModelTable callingProcess = new ProcessModelTable();
		callingProcess.setId(CALLING_PROCESS_ID);
		ProcessModelTable calledProcess = new ProcessModelTable();
		calledProcess.setId(CALLED_PROCESS_ID);

		MessageFlowTable table = new MessageFlowTable();
		table.setBpmnId(BPMN_ID);
		table.setName(NAME);
		table.setDescription(DESCRIPTION);
		table.setCallingProcess(callingProcess);
		table.setCalledProcess(calledProcess);
		table.setCallingElementType(CALLING_ELEMENT_TYPE);
		table.setCalledElementType(CALLED_ELEMENT_TYPE);
		return table;
	}
}
