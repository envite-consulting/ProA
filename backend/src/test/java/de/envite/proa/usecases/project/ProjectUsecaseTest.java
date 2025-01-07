package de.envite.proa.usecases.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import de.envite.proa.entities.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProjectUsecaseTest {

    private static final long PROJECT_ID = 1L;
    private static final String PROJECT_NAME = "Test Project";
    private static final String PROJECT_VERSION = "1.0";
    private static final long USER_ID = 1L;

    private Project mockProject;

    @Mock
    private ProjectRepository repository;

    @InjectMocks
    private ProjectUsecase projectUsecase;



    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockProject = new Project();
        mockProject.setId(PROJECT_ID);
        mockProject.setName(PROJECT_NAME);
        mockProject.setVersion(PROJECT_VERSION);
    }

    @Test
    public void testCreateProject() {
        when(repository.createProject(PROJECT_NAME, PROJECT_VERSION)).thenReturn(mockProject);

        Project result = projectUsecase.createProject(PROJECT_NAME, PROJECT_VERSION);

        assertEquals(mockProject, result);
        verify(repository, times(1)).createProject(PROJECT_NAME, PROJECT_VERSION);
    }

    @Test
    public void testCreateProjectWithUserId() {
        when(repository.createProject(USER_ID, PROJECT_NAME, PROJECT_VERSION)).thenReturn(mockProject);

        Project result = projectUsecase.createProject(USER_ID, PROJECT_NAME, PROJECT_VERSION);

        assertEquals(mockProject, result);
        verify(repository, times(1)).createProject(USER_ID, PROJECT_NAME, PROJECT_VERSION);
    }

    @Test
    public void testGetProjects() {
        List<Project> mockProjects = Collections.singletonList(mockProject);
        when(repository.getProjects()).thenReturn(mockProjects);

        List<Project> result = projectUsecase.getProjects();

        assertEquals(mockProjects, result);
        verify(repository, times(1)).getProjects();
    }

    @Test
    public void testGetProjectsWithUserId() {
        List<Project> mockProjects = Collections.singletonList(mockProject);
        when(repository.getProjects(USER_ID)).thenReturn(mockProjects);

        List<Project> result = projectUsecase.getProjects(USER_ID);

        assertEquals(mockProjects, result);
        verify(repository, times(1)).getProjects(USER_ID);
    }

    @Test
    public void testGetProjectById() {
        when(repository.getProject(PROJECT_ID)).thenReturn(mockProject);

        Project result = projectUsecase.getProject(PROJECT_ID);

        assertEquals(mockProject, result);
        verify(repository, times(1)).getProject(PROJECT_ID);
    }

    @Test
    public void testGetProjectByUserIdAndProjectId() {
        when(repository.getProject(USER_ID, PROJECT_ID)).thenReturn(mockProject);

        Project result = projectUsecase.getProject(USER_ID, PROJECT_ID);

        assertEquals(mockProject, result);
        verify(repository, times(1)).getProject(USER_ID, PROJECT_ID);
    }
}
