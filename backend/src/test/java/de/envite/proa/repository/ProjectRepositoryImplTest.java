package de.envite.proa.repository;

import de.envite.proa.entities.project.AccessDeniedException;
import de.envite.proa.entities.project.NoResultException;
import de.envite.proa.entities.project.Project;
import de.envite.proa.entities.project.ProjectRole;
import de.envite.proa.repository.project.ProjectDao;
import de.envite.proa.repository.project.ProjectRepositoryImpl;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.ProjectUserRelationTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.repository.user.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

		verify(projectDao).persist(any(ProjectTable.class));
	}

	@Test
	void testCreateProjectWithUser() {
		UserTable user = new UserTable();
		user.setId(USER_ID);

		when(userDao.findById(USER_ID)).thenReturn(user);
		doNothing().when(projectDao).persist(any(ProjectVersionTable.class));

		Project project = projectRepository.createProject(USER_ID, PROJECT_NAME, PROJECT_VERSION);

		assertNotNull(project);
		assertEquals(PROJECT_NAME, project.getName());

		ArgumentCaptor<ProjectTable> projectCatpor =  ArgumentCaptor.forClass(ProjectTable.class);
		verify(projectDao).persist(projectCatpor.capture());
		
		assertThat(projectCatpor.getValue().getName()).isEqualTo(PROJECT_NAME);
		assertThat(projectCatpor.getValue().getCreatedAt()).isEqualTo(project.getCreatedAt());
		assertThat(projectCatpor.getValue().getModifiedAt()).isEqualTo(project.getModifiedAt());
		assertThat(projectCatpor.getValue().getUserRelations().stream().findFirst().get().getUser().getId()).isEqualTo(USER_ID);
		assertThat(projectCatpor.getValue().getUserRelations().stream().findFirst().get().getRole()).isEqualTo(ProjectRole.OWNER);
	}

	@Test
	void testGetProjects() {
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);
		when(projectDao.getProjectsWithVersionsAndContributors()).thenReturn(List.of(projectTable));

		List<Project> projects = projectRepository.getProjects();

		assertFalse(projects.isEmpty());
		assertTrue(projects.stream().anyMatch(p -> p.getId().equals(projectTable.getId())));

		verify(projectDao).getProjectsWithVersionsAndContributors();
	}

	@Test
	void testGetProjectsForUser() {

		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		when(projectDao.getAllProjectsForUserWithVersionsAndContributors(any())).thenReturn(List.of(projectTable));

		List<Project> projects = projectRepository.getProjects(USER_ID);
		assertFalse(projects.isEmpty());
		assertTrue(projects.stream().anyMatch(p -> p.getId().equals(projectTable.getId())));

		ArgumentCaptor<UserTable> userCaptor = ArgumentCaptor.forClass(UserTable.class);
		verify(projectDao).getAllProjectsForUserWithVersionsAndContributors(userCaptor.capture());
		assertEquals(USER_ID, userCaptor.getValue().getId());
	}

	@Test
	void testGetProjectById() {
		ProjectTable projectTable = new ProjectTable();
		when(projectDao.findByIdWithVersionsAndContributors(projectTable.getId())).thenReturn(projectTable);

		Project retrievedProject = projectRepository.getProject(projectTable.getId());
		assertNotNull(retrievedProject);
		assertEquals(projectTable.getId(), retrievedProject.getId());

		verify(projectDao).findByIdWithVersionsAndContributors(projectTable.getId());
	}

	@Test
	void testGetProjectById_NotFound() {
		when(projectDao.findByIdWithVersionsAndContributors(PROJECT_ID)).thenReturn(null);

		assertThrows(NoResultException.class, () -> projectRepository.getProject(PROJECT_ID));

		verify(projectDao).findByIdWithVersionsAndContributors(PROJECT_ID);
	}

	@Test
	void testGetProjectByUserAndId() {

		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);

		UserTable user = new UserTable();
		user.setId(USER_ID);
		
		ProjectUserRelationTable relation = new ProjectUserRelationTable();
		relation.setUser(user);
		
		projectTable.getUserRelations().add(relation);

		when(projectDao.findByIdWithVersionsAndContributors(projectTable.getId())).thenReturn(projectTable);

		Project retrievedProject = projectRepository.getProject(USER_ID, projectTable.getId());
		
		assertNotNull(retrievedProject);
		assertEquals(projectTable.getId(), retrievedProject.getId());

		verify(projectDao).findByIdWithVersionsAndContributors(projectTable.getId());
	}

	@Test
	void testGetProjectByUserAndId_Forbidden() {

		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(PROJECT_ID);
		
		when(projectDao.findByIdWithVersionsAndContributors(PROJECT_ID)).thenReturn(projectTable);
		
		assertThrows(AccessDeniedException.class,
				() -> projectRepository.getProject(USER_ID, projectTable.getId()));
	}
}
