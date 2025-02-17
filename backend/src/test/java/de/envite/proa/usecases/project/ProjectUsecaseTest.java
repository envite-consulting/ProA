package de.envite.proa.usecases.project;

import de.envite.proa.entities.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ProjectUsecaseTest {

	private static final long PROJECT_ID = 1L;
	private static final String PROJECT_NAME = "Test Project";
	private static final String PROJECT_VERSION = "1.0";
	private static final long USER_ID = 1L;

	private Project expectedProject;

	@Mock
	private ProjectRepository repository;

	@InjectMocks
	private ProjectUsecase projectUsecase;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		expectedProject = new Project();
		expectedProject.setId(PROJECT_ID);
		expectedProject.setName(PROJECT_NAME);
		expectedProject.setVersion(PROJECT_VERSION);
	}

	@Test
	public void testCreateProject() {
		when(repository.createProject(PROJECT_NAME, PROJECT_VERSION)).thenReturn(expectedProject);

		Project result = projectUsecase.createProject(PROJECT_NAME, PROJECT_VERSION);

		assertEquals(expectedProject, result);
		verify(repository, times(1)).createProject(PROJECT_NAME, PROJECT_VERSION);
	}

	@Test
	public void testCreateProjectWithUserId() {
		when(repository.createProject(USER_ID, PROJECT_NAME, PROJECT_VERSION)).thenReturn(expectedProject);

		Project result = projectUsecase.createProject(USER_ID, PROJECT_NAME, PROJECT_VERSION);

		assertEquals(expectedProject, result);
		verify(repository, times(1)).createProject(USER_ID, PROJECT_NAME, PROJECT_VERSION);
	}

	@Test
	public void testGetProjects() {
		List<Project> mockProjects = Collections.singletonList(expectedProject);
		when(repository.getProjects()).thenReturn(mockProjects);

		List<Project> result = projectUsecase.getProjects();

		assertEquals(mockProjects, result);
		verify(repository, times(1)).getProjects();
	}

	@Test
	public void testGetProjectsWithUserId() {
		List<Project> mockProjects = Collections.singletonList(expectedProject);
		when(repository.getProjects(USER_ID)).thenReturn(mockProjects);

		List<Project> result = projectUsecase.getProjects(USER_ID);

		assertEquals(mockProjects, result);
		verify(repository, times(1)).getProjects(USER_ID);
	}

	@Test
	public void testGetProjectById() {
		when(repository.getProject(PROJECT_ID)).thenReturn(expectedProject);

		Project result = projectUsecase.getProject(PROJECT_ID);

		assertEquals(expectedProject, result);
		verify(repository, times(1)).getProject(PROJECT_ID);
	}

	@Test
	public void testGetProjectByUserIdAndProjectId() {
		when(repository.getProject(USER_ID, PROJECT_ID)).thenReturn(expectedProject);

		Project result = projectUsecase.getProject(USER_ID, PROJECT_ID);

		assertEquals(expectedProject, result);
		verify(repository, times(1)).getProject(USER_ID, PROJECT_ID);
	}

	@Test
	public void testDeleteProject_DesktopMode() {
		doNothing().when(repository).deleteProject(USER_ID);

		projectUsecase.deleteProject(USER_ID);

		verify(repository, times(1)).deleteProject(USER_ID);
	}

	@Test
	public void testDeleteProject_WebMode() {
		doNothing().when(repository).deleteProject(USER_ID, PROJECT_ID);

		projectUsecase.deleteProject(USER_ID, PROJECT_ID);

		verify(repository, times(1)).deleteProject(USER_ID, PROJECT_ID);
	}
}
