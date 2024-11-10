package de.envite.proa.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.Project;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.usecases.project.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProjectRepositoryImpl implements ProjectRepository {

	@Inject
	private ProjectDao projectDao;

	@Inject
	private UserDao userDao;

	@Inject
	public ProjectRepositoryImpl(ProjectDao dao) {
		this.projectDao = dao;
	}

	@Transactional
	@Override
	public Project createProject(String name, String version) {
		ProjectTable table = createProjectTable(name, version);
		projectDao.persist(table);
		return map(table);
	}

	@Transactional
	@Override
	public Project createProject(Long userId, String name, String version) {
		ProjectTable table = createProjectTable(name, version);
		UserTable user = userDao.findById(userId);
		table.setUser(user);
		projectDao.persist(table);
		return map(table);
	}

	private ProjectTable createProjectTable(String name, String version) {
		ProjectTable table = new ProjectTable();
		table.setName(name);
		table.setVersion(version);
		table.setCreatedAt(LocalDateTime.now());
		table.setModifiedAt(LocalDateTime.now());
		return table;
	}

	@Transactional
	@Override
	public List<Project> getProjects() {
		return projectDao//
				.getProjects()//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	@Transactional
	@Override
	public List<Project> getProjects(Long userId) {
		UserTable user =userDao.findById(userId);
		return projectDao//
				.getProjectsForUser(user)//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	@Override
	public Project getProject(Long projectId) {
		return map(projectDao.findById(projectId));
	}

	@Override
	public Project getProject(Long userId, Long projectId) {
		UserTable user = userDao.findById(userId);
		return map(projectDao.findByUserAndId(user, projectId));
	}

	@Override
	public void deleteProject(Long projectId) {
		projectDao.deleteProject(projectId);
	}

	private Project map(ProjectTable table) {

		Project project = new Project();
		project.setId(table.getId());
		project.setName(table.getName());
		project.setVersion(table.getVersion());
		project.setCreatedAt(table.getCreatedAt());
		project.setModifiedAt(table.getModifiedAt());

		return project;
	}
}
