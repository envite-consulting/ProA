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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ProcessModelDaoTest {

	private static final String BPMN_PROCESS_ID_1 = "bpmn-process-id-1";
	private static final String BPMN_PROCESS_ID_2 = "bpmn-process-id-2";
	private static final String PROCESS_MODEL_NAME_1 = "process-model-name-1";
	private static final String PROCESS_MODEL_NAME_2 = "process-model-name-2";
	private static final String PROCESS_MODEL_NAME_3 = "process-model-name-3";
	private static final String BPMN_XML_STRING = "<?xml version=\"1.0\"?>";

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
		processModel.setBpmnXml(BPMN_XML_STRING.getBytes());
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
	void testFindByNameOrBpmnProcessIdWithoutCollaborations() {
		ProcessModelTable retrievedProcessModel = processModelDao.findByNameOrBpmnProcessIdWithoutCollaborations(
				PROCESS_MODEL_NAME_1, BPMN_PROCESS_ID_1, project);
		assertNotNull(retrievedProcessModel);
		assertEquals(BPMN_PROCESS_ID_1, retrievedProcessModel.getBpmnProcessId());
	}

	@Test
	@Transactional
	void testFindByNameOrBpmnProcessIdWithoutCollaborations_NotFound() {
		ProcessModelTable retrievedProcessModel = processModelDao.findByNameOrBpmnProcessIdWithoutCollaborations(
				PROCESS_MODEL_NAME_2, BPMN_PROCESS_ID_2, project);
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
		assertTrue(result.getChildren().stream().map(ProcessModelTable::getName).toList()
				.contains(processModel2.getName()));
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
	void testGetProcessModelsWithChildren() {
		ProcessModelTable child = new ProcessModelTable();
		child.setProject(project);
		child.setName(PROCESS_MODEL_NAME_3);
		em.persist(child);

		processModel.getChildren().add(child);
		em.merge(processModel);

		flushAndClear();

		List<ProcessModelTable> result = processModelDao.getProcessModelsWithChildren(project);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.stream().map(ProcessModelTable::getName).toList()
				.containsAll(Stream.of(processModel, child).map(ProcessModelTable::getName).toList()));

		for (ProcessModelTable pm : result) {
			if (pm.getName().equals(processModel.getName())) {
				assertTrue(pm.getParents().isEmpty());
				assertTrue(
						pm.getChildren().stream().map(ProcessModelTable::getName).toList().contains(child.getName()));
			} else if (pm.getName().equals(child.getName())) {
				assertTrue(pm.getChildren().isEmpty());
				assertTrue(pm.getParents().stream().map(ProcessModelTable::getName).toList()
						.contains(processModel.getName()));
			}
		}
	}

	@Test
	@Transactional
	void testAddChild() {
		ProcessModelTable child = new ProcessModelTable();
		child.setProject(project);
		child.setName(PROCESS_MODEL_NAME_2);
		child.setBpmnProcessId(BPMN_PROCESS_ID_2);
		em.persist(child);

		processModelDao.addChild(processModel.getId(), child.getId());

		flushAndClear();

		ProcessModelTable fetchedParent = processModelDao.findWithChildren(processModel.getId());
		ProcessModelTable fetchedChild = processModelDao.findWithParents(child.getId());

		assertTrue(fetchedParent.getChildren().stream().map(ProcessModelTable::getId).toList().contains(child.getId()));
		assertTrue(fetchedChild.getParents().stream().map(ProcessModelTable::getId).toList()
				.contains(processModel.getId()));
	}

	@Test
	@Transactional
	void testRemoveChild() {
		ProcessModelTable child = new ProcessModelTable();
		child.setProject(project);
		child.setName(PROCESS_MODEL_NAME_2);
		child.setBpmnProcessId(BPMN_PROCESS_ID_2);
		em.persist(child);

		processModelDao.addChild(processModel.getId(), child.getId());

		flushAndClear();

		processModelDao.removeChild(processModel.getId(), child.getId());

		flushAndClear();

		ProcessModelTable fetchedParent = processModelDao.findWithChildren(processModel.getId());
		ProcessModelTable fetchedChild = processModelDao.findWithParents(child.getId());

		assertFalse(
				fetchedParent.getChildren().stream().map(ProcessModelTable::getId).toList().contains(child.getId()));
		assertFalse(fetchedChild.getParents().stream().map(ProcessModelTable::getId).toList()
				.contains(processModel.getId()));
	}

	@Test
	@Transactional
	void testGetBpmnXml() {
		byte[] actualBpmnXml = processModelDao.getBpmnXml(processModel.getId());
		byte[] expectedBpmnXml = processModel.getBpmnXml();

		assertArrayEquals(expectedBpmnXml, actualBpmnXml);
	}

	@Test
	@Transactional
	void testFindWithParents() {
		ProcessModelTable parent = new ProcessModelTable();
		em.persist(parent);

		processModelDao.addChild(parent.getId(), processModel.getId());

		flushAndClear();

		processModelDao.findWithParents(processModel.getId());

		flushAndClear();

		ProcessModelTable fetchedParent = processModelDao.findWithChildren(parent.getId());
		ProcessModelTable fetchedChild = processModelDao.findWithParents(processModel.getId());

		assertTrue(fetchedParent.getChildren().stream().map(ProcessModelTable::getId).toList()
				.contains(processModel.getId()));
		assertTrue(fetchedChild.getParents().stream().map(ProcessModelTable::getId).toList()
				.contains(parent.getId()));
	}
}
