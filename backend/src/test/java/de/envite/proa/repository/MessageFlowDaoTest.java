package de.envite.proa.repository;

import de.envite.proa.repository.messageflow.MessageFlowDao;
import de.envite.proa.repository.tables.MessageFlowTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class MessageFlowDaoTest {

	private static final String PROJECT_NAME = "Test Project";
	private static final String PROCESS_MODEL_NAME = "Test Process Model";
	private static final String MESSAGE_FLOW_NAME_1 = "Test Message Flow 1";
	private static final String MESSAGE_FLOW_NAME_2 = "Test Message Flow 2";

	@Inject
	EntityManager em;

	@Inject
	MessageFlowDao messageFlowDao;

	private ProjectVersionTable project;
	private ProcessModelTable processModel;
	private MessageFlowTable messageFlow;

	@BeforeEach
	@Transactional
	void setUp() {
		project = new ProjectVersionTable();
		project.setName(PROJECT_NAME);
		em.persist(project);

		processModel = new ProcessModelTable();
		processModel.setName(PROCESS_MODEL_NAME);
		processModel.setProject(project);
		em.persist(processModel);

		messageFlow = new MessageFlowTable();
		messageFlow.setName(MESSAGE_FLOW_NAME_1);
		messageFlow.setProject(project);
		messageFlow.setCallingProcess(processModel);
		messageFlowDao.persist(messageFlow);
	}

	@AfterEach
	@Transactional
	void cleanup() {
		em.createQuery("DELETE FROM MessageFlowTable").executeUpdate();
		em.createQuery("DELETE FROM ProcessModelTable").executeUpdate();
		em.createQuery("DELETE FROM ProjectVersionTable").executeUpdate();
	}

	private void flushAndClear() {
		em.flush();
		em.clear();
	}

	@Test
	@Transactional
	void testPersistMessageFlow() {
		flushAndClear();

		MessageFlowTable savedMessageFlow = em.find(MessageFlowTable.class, messageFlow.getId());
		assertNotNull(savedMessageFlow);
		assertEquals(MESSAGE_FLOW_NAME_1, savedMessageFlow.getName());
	}

	@Test
	@Transactional
	void testGetMessageFlowsByProject() {
		List<MessageFlowTable> messageFlows = messageFlowDao.getMessageFlows(project);
		assertNotNull(messageFlows);
		assertFalse(messageFlows.isEmpty());
		assertEquals(MESSAGE_FLOW_NAME_1, messageFlows.getFirst().getName());
	}

	@Test
	@Transactional
	void testGetMessageFlowsByProjectAndProcessModel() {
		List<MessageFlowTable> messageFlows = messageFlowDao.getMessageFlows(project, processModel);
		assertNotNull(messageFlows);
		assertFalse(messageFlows.isEmpty());
		assertEquals(MESSAGE_FLOW_NAME_1, messageFlows.getFirst().getName());
	}

	@Test
	@Transactional
	void testMergeMessageFlow() {
		messageFlow.setName(MESSAGE_FLOW_NAME_2);
		messageFlowDao.merge(messageFlow);

		flushAndClear();

		MessageFlowTable updatedMessageFlow = em.find(MessageFlowTable.class, messageFlow.getId());
		assertNotNull(updatedMessageFlow);
		assertEquals(MESSAGE_FLOW_NAME_2, updatedMessageFlow.getName());
	}
}
