package de.envite.proa.repository;

import de.envite.proa.entities.process.EventType;
import de.envite.proa.repository.processmodel.ProcessEventDao;
import de.envite.proa.repository.tables.ProcessEventTable;
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
public class ProcessEventDaoTest {

	private static final String PROJECT_NAME = "Test Project";
	private static final String EVENT_LABEL_1 = "Event 1";
	private static final String EVENT_LABEL_2 = "Event 2";

	@Inject
	ProcessEventDao processEventDao;

	@Inject
	EntityManager em;

	private ProjectVersionTable project;
	private ProcessModelTable processModel;

	@BeforeEach
	@Transactional
	void setUp() {
		project = new ProjectVersionTable();
		project.setName(PROJECT_NAME);
		em.persist(project);

		processModel = new ProcessModelTable();
		processModel.setProject(project);
		em.persist(processModel);
	}

	@AfterEach
	@Transactional
	void cleanupDatabase() {
		em.createQuery("DELETE FROM ProcessEventTable").executeUpdate();
		em.createQuery("DELETE FROM ProcessModelTable").executeUpdate();
		em.createQuery("DELETE FROM ProjectVersionTable").executeUpdate();
	}

	@Test
	@Transactional
	void testGetEventsForLabelAndType() {
		ProcessEventTable event1 = new ProcessEventTable();
		event1.setLabel(EVENT_LABEL_1);
		event1.setEventType(EventType.START);
		event1.setProject(project);
		em.persist(event1);

		ProcessEventTable event2 = new ProcessEventTable();
		event2.setLabel(EVENT_LABEL_2);
		event2.setEventType(EventType.END);
		event2.setProject(project);
		em.persist(event2);

		List<ProcessEventTable> events = processEventDao.getEventsForLabelAndType(EVENT_LABEL_1, EventType.START,
				project);
		assertNotNull(events);
		assertEquals(1, events.size());
		assertEquals(EVENT_LABEL_1, events.getFirst().getLabel());
		assertEquals(EventType.START, events.getFirst().getEventType());
	}

	@Test
	@Transactional
	void testGetEventsForLabelAndTypeNotFound() {
		List<ProcessEventTable> events = processEventDao.getEventsForLabelAndType(EVENT_LABEL_1, EventType.START,
				project);
		assertNotNull(events);
		assertTrue(events.isEmpty());
	}

	@Test
	@Transactional
	void testFindForProcessModelAndEventType() {
		ProcessEventTable event = new ProcessEventTable();
		event.setLabel(EVENT_LABEL_1);
		event.setEventType(EventType.START);
		event.setProcessModel(processModel);
		em.persist(event);

		ProcessEventTable foundEvent = processEventDao.findForProcessModelAndEventType(processModel, EventType.START);
		assertNotNull(foundEvent);
		assertEquals(EVENT_LABEL_1, foundEvent.getLabel());
		assertEquals(EventType.START, foundEvent.getEventType());
	}

	@Test
	@Transactional
	void testFindForProcessModelAndEventTypeNotFound() {
		ProcessEventTable foundEvent = processEventDao.findForProcessModelAndEventType(processModel, EventType.END);
		assertNull(foundEvent);
	}
}
