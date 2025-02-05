package de.envite.proa.repository;

import de.envite.proa.repository.processmodel.ProcessModelDao;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ProcessModelDaoTest {

	private static final String BPMN_PROCESS_ID_1 = "bpmn-process-id-1";
	private static final String BPMN_PROCESS_ID_2 = "bpmn-process-id-2";
	private static final String PROCESS_MODEL_NAME_1 = "process-model-name-1";
	private static final String PROCESS_MODEL_NAME_2 = "process-model-name-2";

	@Inject
	EntityManager em;

	@Inject
	ProcessModelDao processModelDao;

	private ProjectTable project;
	private ProcessModelTable processModel;

	@BeforeEach
	@Transactional
	void setUp() {
		project = new ProjectTable();
		em.persist(project);

		processModel = new ProcessModelTable();
		processModel.setProject(project);
		processModel.setName(PROCESS_MODEL_NAME_1);
		processModel.setBpmnProcessId(BPMN_PROCESS_ID_1);
		em.persist(processModel);
	}

	@AfterEach
	@Transactional
	void cleanup() {
		em.createQuery("DELETE FROM ProcessModelTable").executeUpdate();
		em.createQuery("DELETE FROM ProjectTable").executeUpdate();
	}

	private void flushAndClear() {
		em.flush();
		em.clear();
	}

	@Test
	@Transactional
	void testGetProcessModels() {
		List<ProcessModelTable> processModels = processModelDao.getProcessModels(project);
		assertNotNull(processModels);
		assertFalse(processModels.isEmpty());
	}

	@Test
	@Transactional
	void testGetProcessModelsForName() {
		List<ProcessModelTable> processModels = processModelDao.getProcessModelsForName(PROCESS_MODEL_NAME_1, project);
		assertNotNull(processModels);
		assertFalse(processModels.isEmpty());
	}

	@Test
	@Transactional
	void testPersistProcessModel() {
		ProcessModelTable persistedProcessModel = em.find(ProcessModelTable.class, processModel.getId());
		assertNotNull(persistedProcessModel);
		assertEquals(processModel.getId(), persistedProcessModel.getId());
	}

	@Test
	@Transactional
	void testMergeProcessModel() {
		processModel.setName(PROCESS_MODEL_NAME_2);
		processModelDao.merge(processModel);

		flushAndClear();

		ProcessModelTable retrievedProcessModel = em.find(ProcessModelTable.class, processModel.getId());
		assertEquals(PROCESS_MODEL_NAME_2, retrievedProcessModel.getName());
	}

	@Test
	@Transactional
	void testFindWithChildren() {
		ProcessModelTable retrievedProcessModel = processModelDao.findWithChildren(processModel.getId());
		assertNotNull(retrievedProcessModel);
		assertNotNull(retrievedProcessModel.getEvents());
		assertNotNull(retrievedProcessModel.getCallActivites());
	}

	@Test
	@Transactional
	void testFindByBpmnProcessId() {
		ProcessModelTable retrievedProcessModel = processModelDao.findByBpmnProcessId(BPMN_PROCESS_ID_1, project);
		assertNotNull(retrievedProcessModel);
		assertEquals(BPMN_PROCESS_ID_1, retrievedProcessModel.getBpmnProcessId());
	}

	@Test
	@Transactional
	void testFindByBpmnProcessIdNotFound() {
		ProcessModelTable retrievedProcessModel = processModelDao.findByBpmnProcessId(BPMN_PROCESS_ID_2, project);
		assertNull(retrievedProcessModel);
	}

	@Test
	@Transactional
	void testFindByName() {
		ProcessModelTable retrievedProcessModel = processModelDao.findByName(PROCESS_MODEL_NAME_1, project);
		assertNotNull(retrievedProcessModel);
		assertEquals(PROCESS_MODEL_NAME_1, retrievedProcessModel.getName());
	}

	@Test
	@Transactional
	void testFindByNameNotFound() {
		ProcessModelTable retrievedProcessModel = processModelDao.findByName(PROCESS_MODEL_NAME_2, project);
		assertNull(retrievedProcessModel);
	}

	@Test
	@Transactional
	void testDelete() {
		processModelDao.delete(processModel.getId());

		flushAndClear();

		ProcessModelTable retrievedProcessModel = em.find(ProcessModelTable.class, processModel.getId());
		assertNull(retrievedProcessModel);
	}

	@Test
	@Transactional
	void testDeleteMultiple() {
		ProcessModelTable processModel2 = new ProcessModelTable();
		processModel2.setProject(project);
		processModel2.setName(PROCESS_MODEL_NAME_2);
		em.persist(processModel2);

		processModelDao.delete(Arrays.asList(processModel.getId(), processModel2.getId()));

		flushAndClear();

		ProcessModelTable retrievedProcessModel1 = em.find(ProcessModelTable.class, processModel.getId());
		ProcessModelTable retrievedProcessModel2 = em.find(ProcessModelTable.class, processModel2.getId());

		assertNull(retrievedProcessModel1);
		assertNull(retrievedProcessModel2);
	}
}
