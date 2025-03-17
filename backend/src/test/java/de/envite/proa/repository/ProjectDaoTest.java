package de.envite.proa.repository;

import de.envite.proa.repository.project.ProjectDao;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import de.envite.proa.repository.tables.UserTable;
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
class ProjectDaoTest {

	private static final String USER_EMAIL = "user@example.com";
	private static final String PROJECT_NAME = "Project 1";
	private static final String VERSION_NAME = "Version 1";

	@Inject
	EntityManager em;

	@Inject
	ProjectDao projectDao;

	private UserTable user;
	private ProjectTable project;

	@BeforeEach
	@Transactional
	void setUp() {
		user = new UserTable();
		user.setEmail(USER_EMAIL);
		em.persist(user);

		project = new ProjectTable();
		project.setName(PROJECT_NAME);
		project.setOwner(user);

		ProjectVersionTable projectVersion = new ProjectVersionTable();
		projectVersion.setName(VERSION_NAME);
		em.persist(projectVersion);

		project.getVersions().add(projectVersion);

		em.persist(project);
	}

	@AfterEach
	@Transactional
	void cleanup() {
		em.createQuery("DELETE FROM ProjectVersionTable").executeUpdate();
		em.createQuery("DELETE FROM UserTable").executeUpdate();
	}

	@Test
	@Transactional
	void testPersistProject() {
		ProjectVersionTable savedProject = em.find(ProjectVersionTable.class, project.getId());
		assertNotNull(savedProject);
		assertEquals(PROJECT_NAME, savedProject.getName());
	}

	@Test
	@Transactional
	void testGetProjects() {
		List<ProjectTable> projects = projectDao.getProjects();
		assertNotNull(projects);
		assertFalse(projects.isEmpty());
	}

	@Test
	@Transactional
	void testGetProjectsForUser() {
		List<ProjectTable> projects = projectDao.getProjectsForUser(user);
		assertNotNull(projects);
		assertFalse(projects.isEmpty());
		assertEquals(PROJECT_NAME, projects.getFirst().getName());
	}

	@Test
	@Transactional
	void testFindVersionById() {
		ProjectVersionTable foundProjectVersion = projectDao.findVersionById(project.getId());
		assertNotNull(foundProjectVersion);
		assertEquals(PROJECT_NAME, foundProjectVersion.getName());
	}
}
