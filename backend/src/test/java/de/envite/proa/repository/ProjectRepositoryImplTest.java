package de.envite.proa.repository;

import de.envite.proa.entities.project.Project;
import de.envite.proa.repository.project.ProjectDao;
import de.envite.proa.repository.project.ProjectRepositoryImpl;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.repository.user.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectRepositoryImplTest {

	private static final Long PROJECT_ID = 1L;
	private static final String PROJECT_NAME = "Test Project";
	private static final String PROJECT_VERSION = "1.0";
	private static final Long USER_ID = 1L;

	@InjectMocks
	ProjectRepositoryImpl projectRepository;

	@Mock
	ProjectDao projectDao;

	@Mock
	UserDao userDao;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testCreateProject() {
		doNothing().when(projectDao).persist(any(ProjectTable.class));

		Project project = projectRepository.createProject(PROJECT_NAME, PROJECT_VERSION);

		assertNotNull(project);
		assertEquals(PROJECT_NAME, project.getName());
		assertEquals(PROJECT_VERSION, project.getVersion());

		verify(projectDao).persist(any(ProjectTable.class));
	}

	@Test
	void testCreateProjectWithUser() {
		UserTable user = new UserTable();
		user.setId(USER_ID);

		when(userDao.findById(USER_ID)).thenReturn(user);
		doNothing().when(projectDao).persist(any(ProjectTable.class));

		Project project = projectRepository.createProject(USER_ID, PROJECT_NAME, PROJECT_VERSION);

		assertNotNull(project);
		assertEquals(PROJECT_NAME, project.getName());
		assertEquals(PROJECT_VERSION, project.getVersion());

		ProjectTable projectArgument = new ProjectTable();
		projectArgument.setUser(user);
		projectArgument.setName(PROJECT_NAME);
		projectArgument.setVersion(PROJECT_VERSION);
		projectArgument.setCreatedAt(project.getCreatedAt());
		projectArgument.setModifiedAt(project.getModifiedAt());

		verify(userDao).findById(USER_ID);
		verify(projectDao).persist(projectArgument);
	}

	@Test
	void testGetProjects() {
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);
		when(projectDao.getProjects()).thenReturn(List.of(projectTable));

		List<Project> projects = projectRepository.getProjects();

		assertFalse(projects.isEmpty());
		assertTrue(projects.stream().anyMatch(p -> p.getId().equals(projectTable.getId())));

		verify(projectDao).getProjects();
	}

	@Test
	void testGetProjectsForUser() {
		UserTable user = new UserTable();
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);
		when(userDao.findById(USER_ID)).thenReturn(user);
		when(projectDao.getProjectsForUser(user)).thenReturn(List.of(projectTable));

		List<Project> projects = projectRepository.getProjects(USER_ID);
		assertFalse(projects.isEmpty());
		assertTrue(projects.stream().anyMatch(p -> p.getId().equals(projectTable.getId())));

		verify(userDao).findById(USER_ID);
		verify(projectDao).getProjectsForUser(user);
	}

	@Test
	void testGetProjectById() {
		ProjectTable projectTable = new ProjectTable();
		when(projectDao.findById(projectTable.getId())).thenReturn(projectTable);

		Project retrievedProject = projectRepository.getProject(projectTable.getId());
		assertNotNull(retrievedProject);
		assertEquals(projectTable.getId(), retrievedProject.getId());

		verify(projectDao).findById(projectTable.getId());
	}

	@Test
	void testGetProjectByUserAndId() {
		UserTable user = new UserTable();
		ProjectTable projectTable = new ProjectTable();
		when(userDao.findById(USER_ID)).thenReturn(user);
		when(projectDao.findById(projectTable.getId())).thenReturn(projectTable);
		when(projectDao.findByUserAndId(user, projectTable.getId())).thenReturn(projectTable);

		Project retrievedProject = projectRepository.getProject(USER_ID, projectTable.getId());
		assertNotNull(retrievedProject);
		assertEquals(projectTable.getId(), retrievedProject.getId());

		verify(userDao).findById(USER_ID);
		verify(projectDao).findById(projectTable.getId());
		verify(projectDao).findByUserAndId(user, projectTable.getId());
	}
}
