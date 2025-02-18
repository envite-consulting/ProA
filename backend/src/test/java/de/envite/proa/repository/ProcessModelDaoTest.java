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
	private static final String PROCESS_MODEL_NAME_3 = "process-model-name-3";

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
		List<ProcessModelTable> processModels = processModelDao.getProcessModels(project, null);
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

	@Test
	@Transactional
	void findByBpmnProcessIdWithChildren() {
		ProcessModelTable processModel2 = new ProcessModelTable();
		processModel2.setProject(project);
		processModel2.setName(PROCESS_MODEL_NAME_2);
		em.persist(processModel2);

		processModel.getChildren().add(processModel2);
		em.merge(processModel);

		ProcessModelTable result = processModelDao.findByBpmnProcessIdWithChildren(processModel.getBpmnProcessId(),
				project);

		assertNotNull(result);
		assertEquals(processModel.getName(), result.getName());
		assertEquals(processModel2.getName(), result.getChildren().getFirst().getName());
	}

	@Test
	@Transactional
	void findByBpmnProcessIdWithChildren_NotFound() {
		ProcessModelTable result = processModelDao.findByBpmnProcessIdWithChildren(BPMN_PROCESS_ID_2,
				project);

		assertNull(result);
	}

	@Test
	@Transactional
	void testFind() {
		ProcessModelTable result = processModelDao.find(processModel.getId());

		assertNotNull(result);
		assertEquals(processModel.getName(), result.getName());
	}

	@Test
	@Transactional
	void testGetProcessModelsWithParentsAndChildren() {
		ProcessModelTable parent = new ProcessModelTable();
		parent.setProject(project);
		parent.setName(PROCESS_MODEL_NAME_2);
		parent.getChildren().add(processModel);
		em.persist(parent);

		ProcessModelTable child = new ProcessModelTable();
		child.setProject(project);
		child.setName(PROCESS_MODEL_NAME_3);
		em.persist(child);

		processModel.getChildren().add(child);
		em.merge(processModel);

		flushAndClear();

		List<ProcessModelTable> result = processModelDao.getProcessModelsWithParentsAndChildren(project);

		assertNotNull(result);
		assertEquals(3, result.size());
		List<String> processModelNames = result.stream().map(ProcessModelTable::getName).toList();
		assertTrue(processModelNames.contains(processModel.getName()));
		assertTrue(processModelNames.contains(parent.getName()));
		assertTrue(processModelNames.contains(child.getName()));

		for (ProcessModelTable pm : result) {
			if (pm.getName().equals(processModel.getName())) {
				assertEquals(parent.getName(), pm.getParents().getFirst().getName());
				assertEquals(child.getName(), pm.getChildren().getFirst().getName());
			} else if (pm.getName().equals(parent.getName())) {
				assertEquals(processModel.getName(), pm.getChildren().getFirst().getName());
			} else {
				assertEquals(processModel.getName(), pm.getParents().getFirst().getName());
			}
		}
	}
}
