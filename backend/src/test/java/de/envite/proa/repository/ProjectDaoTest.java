package de.envite.proa.repository;

import de.envite.proa.repository.tables.ProjectTable;
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
    private static final String PROJECT_NAME_1 = "Project 1";
    private static final String PROJECT_NAME_2 = "Project 2";

    @Inject
    EntityManager em;

    @Inject
    ProjectDao projectDao;

    private UserTable user;
    private ProjectTable project1;

    @BeforeEach
    @Transactional
    void setUp() {
        user = new UserTable();
        user.setEmail(USER_EMAIL);
        em.persist(user);

        project1 = new ProjectTable();
        project1.setName(PROJECT_NAME_1);
        project1.setUser(user);
        em.persist(project1);

        ProjectTable project2 = new ProjectTable();
        project2.setName(PROJECT_NAME_2);
        project2.setUser(user);
        em.persist(project2);
    }

    @AfterEach
    @Transactional
    void cleanup() {
        em.createQuery("DELETE FROM ProjectTable").executeUpdate();
        em.createQuery("DELETE FROM UserTable").executeUpdate();
    }

    @Test
    @Transactional
    void testPersistProject() {
        ProjectTable savedProject = em.find(ProjectTable.class, project1.getId());
        assertNotNull(savedProject);
        assertEquals(PROJECT_NAME_1, savedProject.getName());
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
        assertEquals(PROJECT_NAME_1, projects.getFirst().getName());
    }

    @Test
    @Transactional
    void testFindById() {
        ProjectTable foundProject = projectDao.findById(project1.getId());
        assertNotNull(foundProject);
        assertEquals(PROJECT_NAME_1, foundProject.getName());
    }

    @Test
    @Transactional
    void testFindByUserAndId() {
        ProjectTable foundProject = projectDao.findByUserAndId(user, project1.getId());
        assertNotNull(foundProject);
        assertEquals(PROJECT_NAME_1, foundProject.getName());
    }
}
