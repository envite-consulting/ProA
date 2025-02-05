package de.envite.proa.rest;

import de.envite.proa.entities.project.Project;
import de.envite.proa.usecases.project.ProjectUsecase;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ProjectResourceTest {

	@InjectMocks
	private ProjectResource resource;

	@Mock
	private ProjectUsecase usecase;

	@Mock
	private JsonWebToken jwt;

	private static final Long USER_ID = 1L;
	private static final Long PROJECT_ID_1 = 1L;
	private static final String PROJECT_NAME_1 = "Test Project 1";
	private static final String PROJECT_VERSION_1 = "1.0";
	private static final Long PROJECT_ID_2 = 2L;
	private static final String PROJECT_NAME_2 = "Test Project 2";
	private static final String PROJECT_VERSION_2 = "3.0";
	private static final String APP_MODE_WEB = "web";
	private static final String APP_MODE_DESKTOP = "desktop";

	private static Project expectedProject1;
	private static Project expectedProject2;

	@BeforeAll
	public static void setUpClass() {
		expectedProject1 = new Project();
		expectedProject1.setId(PROJECT_ID_1);
		expectedProject1.setName(PROJECT_NAME_1);
		expectedProject1.setVersion(PROJECT_VERSION_1);

		expectedProject2 = new Project();
		expectedProject2.setId(PROJECT_ID_2);
		expectedProject2.setName(PROJECT_NAME_2);
		expectedProject2.setVersion(PROJECT_VERSION_2);
	}

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testCreateProjectWebMode() {
		when(jwt.getClaim("userId")).thenReturn(USER_ID.toString());
		resource.appMode = APP_MODE_WEB;

		when(usecase.createProject(USER_ID, PROJECT_NAME_1, PROJECT_VERSION_1)).thenReturn(expectedProject1);

		Project result = resource.createProject(PROJECT_NAME_1, PROJECT_VERSION_1);

		assertEquals(expectedProject1, result);
		verify(jwt, times(1)).getClaim("userId");
		verify(usecase, times(1)).createProject(USER_ID, PROJECT_NAME_1, PROJECT_VERSION_1);
	}

	@Test
	public void testCreateProjectDesktopMode() {
		resource.appMode = APP_MODE_DESKTOP;

		when(usecase.createProject(PROJECT_NAME_1, PROJECT_VERSION_1)).thenReturn(expectedProject1);

		Project result = resource.createProject(PROJECT_NAME_1, PROJECT_VERSION_1);

		assertEquals(expectedProject1, result);
		verify(jwt, never()).getClaim("userId");
		verify(usecase, times(1)).createProject(PROJECT_NAME_1, PROJECT_VERSION_1);
	}

	@Test
	public void testGetProjectsWebMode() {
		when(jwt.getClaim("userId")).thenReturn(USER_ID.toString());
		resource.appMode = APP_MODE_WEB;

		List<Project> expectedProjects = Arrays.asList(
				expectedProject1, expectedProject2
		);

		when(usecase.getProjects(USER_ID)).thenReturn(expectedProjects);

		List<Project> result = resource.getProjects();

		assertEquals(expectedProjects, result);
		verify(jwt, times(1)).getClaim("userId");
		verify(usecase, times(1)).getProjects(USER_ID);
	}

	@Test
	public void testGetProjectsDesktopMode() {
		resource.appMode = APP_MODE_DESKTOP;

		List<Project> expectedProjects = Arrays.asList(
				expectedProject1, expectedProject2
		);

		when(usecase.getProjects()).thenReturn(expectedProjects);

		List<Project> result = resource.getProjects();

		assertEquals(expectedProjects, result);
		verify(jwt, never()).getClaim("userId");
		verify(usecase, times(1)).getProjects();
	}

	@Test
	public void testGetProjectWebMode() {
		when(jwt.getClaim("userId")).thenReturn(USER_ID.toString());
		resource.appMode = APP_MODE_WEB;

		when(usecase.getProject(USER_ID, PROJECT_ID_1)).thenReturn(expectedProject1);

		Response result = resource.getProject(PROJECT_ID_1);

		assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
		assertEquals(expectedProject1, result.getEntity());
		verify(jwt, times(1)).getClaim("userId");
		verify(usecase, times(1)).getProject(USER_ID, PROJECT_ID_1);
	}

	@Test
	public void testGetProjectDesktopMode() {
		resource.appMode = APP_MODE_DESKTOP;

		when(usecase.getProject(PROJECT_ID_1)).thenReturn(expectedProject1);

		Response result = resource.getProject(PROJECT_ID_1);

		assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
		assertEquals(expectedProject1, result.getEntity());
		verify(jwt, never()).getClaim("userId");
		verify(usecase, times(1)).getProject(PROJECT_ID_1);
	}
}
