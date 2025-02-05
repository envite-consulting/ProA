package de.envite.proa.repository;

import de.envite.proa.repository.processmodel.ProcessConnectionDao;
import de.envite.proa.repository.tables.ProcessConnectionTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
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
class ProcessConnectionDaoTest {

	private static final String PROJECT_NAME = "Test Project";
	private static final String PROCESS_MODEL_NAME = "Test Process Model";
	private static final String CONNECTION_LABEL_1 = "Test Connection 1";
	private static final String CONNECTION_LABEL_2 = "Test Connection 2";
	private static final String CONNECTION_LABEL_3 = "Test Connection 3";

	@Inject
	EntityManager em;

	@Inject
	ProcessConnectionDao processConnectionDao;

	private ProjectTable project;
	private ProcessModelTable processModel;
	private ProcessConnectionTable connection1;

	@BeforeEach
	@Transactional
	void setUp() {
		project = new ProjectTable();
		project.setName(PROJECT_NAME);
		em.persist(project);

		processModel = new ProcessModelTable();
		processModel.setName(PROCESS_MODEL_NAME);
		processModel.setProject(project);
		em.persist(processModel);

		connection1 = new ProcessConnectionTable();
		connection1.setLabel(CONNECTION_LABEL_1);
		connection1.setProject(project);
		connection1.setCallingProcess(processModel);
		em.persist(connection1);

		ProcessConnectionTable connection2 = new ProcessConnectionTable();
		connection2.setLabel(CONNECTION_LABEL_2);
		connection2.setProject(project);
		connection2.setCalledProcess(processModel);
		em.persist(connection2);
	}

	@AfterEach
	@Transactional
	void cleanup() {
		em.createQuery("DELETE FROM ProcessConnectionTable").executeUpdate();
		em.createQuery("DELETE FROM ProcessModelTable").executeUpdate();
		em.createQuery("DELETE FROM ProjectTable").executeUpdate();
	}

	private void flushAndClear() {
		em.flush();
		em.clear();
	}

	@Test
	@Transactional
	void testPersistProcessConnection() {
		ProcessConnectionTable savedConnection = em.find(ProcessConnectionTable.class, connection1.getId());
		assertNotNull(savedConnection);
		assertEquals(CONNECTION_LABEL_1, savedConnection.getLabel());
	}

	@Test
	@Transactional
	void testGetProcessConnectionsByProject() {
		List<ProcessConnectionTable> connections = processConnectionDao.getProcessConnections(project);
		assertNotNull(connections);
		assertFalse(connections.isEmpty());
		assertEquals(CONNECTION_LABEL_1, connections.getFirst().getLabel());
	}

	@Test
	@Transactional
	void testGetProcessConnectionsByProjectAndProcessModel() {
		List<ProcessConnectionTable> connections = processConnectionDao.getProcessConnections(project, processModel);
		assertNotNull(connections);
		assertFalse(connections.isEmpty());
		assertEquals(CONNECTION_LABEL_1, connections.getFirst().getLabel());
	}

	@Test
	@Transactional
	void testDeleteForProcessModel() {
		processConnectionDao.deleteForProcessModel(processModel.getId());

		flushAndClear();

		List<ProcessConnectionTable> connections = processConnectionDao.getProcessConnections(project);
		assertTrue(connections.isEmpty());
	}

	@Test
	@Transactional
	void testAddConnection() {
		ProcessConnectionTable newConnection = new ProcessConnectionTable();
		newConnection.setLabel(CONNECTION_LABEL_3);
		newConnection.setProject(project);
		newConnection.setCallingProcess(processModel);

		processConnectionDao.addConnection(newConnection);

		flushAndClear();

		ProcessConnectionTable savedConnection = em.find(ProcessConnectionTable.class, newConnection.getId());
		assertNotNull(savedConnection);
		assertEquals(CONNECTION_LABEL_3, savedConnection.getLabel());
	}

	@Test
	@Transactional
	void testDeleteConnection() {
		processConnectionDao.deleteConnection(connection1.getId());

		flushAndClear();

		ProcessConnectionTable deletedConnection = em.find(ProcessConnectionTable.class, connection1.getId());
		assertNull(deletedConnection);
	}
}
