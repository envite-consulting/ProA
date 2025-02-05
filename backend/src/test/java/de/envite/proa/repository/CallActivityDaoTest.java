package de.envite.proa.repository;

import de.envite.proa.repository.processmodel.CallActivityDao;
import de.envite.proa.repository.tables.CallActivityTable;
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
public class CallActivityDaoTest {

	private static final String PROJECT_NAME = "Test Project";
	private static final String ACTIVITY_LABEL_1 = "Test Activity 1";
	private static final String ACTIVITY_LABEL_2 = "Test Activity 2";

	@Inject
	CallActivityDao callActivityDao;

	@Inject
	EntityManager em;

	private ProjectTable project;
	private ProcessModelTable processModel;

	@BeforeEach
	@Transactional
	void setUp() {
		project = new ProjectTable();
		project.setName(PROJECT_NAME);
		em.persist(project);

		processModel = new ProcessModelTable();
		processModel.setProject(project);
		em.persist(processModel);
	}

	@AfterEach
	@Transactional
	void cleanupDatabase() {
		em.createQuery("DELETE FROM CallActivityTable").executeUpdate();
		em.createQuery("DELETE FROM ProcessModelTable").executeUpdate();
		em.createQuery("DELETE FROM ProjectTable").executeUpdate();
	}

	@Test
	@Transactional
	void testPersistCallActivity() {
		CallActivityTable activity = new CallActivityTable();
		activity.setLabel(ACTIVITY_LABEL_1);
		activity.setProject(project);
		activity.setProcessModel(processModel);
		callActivityDao.persist(activity);

		flushAndClear();

		CallActivityTable persistedActivity = em.find(CallActivityTable.class, activity.getId());
		assertNotNull(persistedActivity);
		assertEquals(ACTIVITY_LABEL_1, persistedActivity.getLabel());
	}

	@Test
	@Transactional
	void testGetCallActivitiesForName() {
		CallActivityTable activity1 = new CallActivityTable();
		activity1.setLabel(ACTIVITY_LABEL_1);
		activity1.setProject(project);
		activity1.setProcessModel(processModel);
		em.persist(activity1);

		CallActivityTable activity2 = new CallActivityTable();
		activity2.setLabel(ACTIVITY_LABEL_2);
		activity2.setProject(project);
		activity2.setProcessModel(processModel);
		em.persist(activity2);

		List<CallActivityTable> activities = callActivityDao.getCallActivitiesForName(ACTIVITY_LABEL_1, project);
		assertNotNull(activities);
		assertEquals(1, activities.size());
		assertEquals(ACTIVITY_LABEL_1, activities.getFirst().getLabel());
	}

	@Test
	@Transactional
	void testGetCallActivitiesForNameNotFound() {
		List<CallActivityTable> activities = callActivityDao.getCallActivitiesForName(ACTIVITY_LABEL_1, project);
		assertNotNull(activities);
		assertTrue(activities.isEmpty());
	}

	@Test
	@Transactional
	void testFindForProcessModel() {
		CallActivityTable activity = new CallActivityTable();
		activity.setLabel(ACTIVITY_LABEL_1);
		activity.setProject(project);
		activity.setProcessModel(processModel);
		em.persist(activity);

		CallActivityTable foundActivity = callActivityDao.findForProcessModel(processModel);
		assertNotNull(foundActivity);
		assertEquals(ACTIVITY_LABEL_1, foundActivity.getLabel());
	}

	@Test
	@Transactional
	void testFindForProcessModelNotFound() {
		ProcessModelTable processModel = new ProcessModelTable();
		processModel.setProject(project);
		em.persist(processModel);

		CallActivityTable foundActivity = callActivityDao.findForProcessModel(processModel);
		assertNull(foundActivity);
	}

	private void flushAndClear() {
		em.flush();
		em.clear();
	}
}
