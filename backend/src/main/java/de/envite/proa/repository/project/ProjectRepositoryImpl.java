package de.envite.proa.repository.project;

import de.envite.proa.entities.project.Project;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.repository.user.UserDao;
import de.envite.proa.usecases.project.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProjectRepositoryImpl implements ProjectRepository {

	@Inject
	private ProjectDao projectDao;

	@Inject
	private UserDao userDao;

	@Inject
	public ProjectRepositoryImpl(ProjectDao dao, UserDao userDao) {
		this.projectDao = dao;
		this.userDao = userDao;
	}

	@Override
	public Project createProject(String name, String version) {
		ProjectTable table = createProjectTable(name, version);
		projectDao.persist(table);
		return map(table);
	}

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

	@Override
	public List<Project> getProjects() {
		return projectDao//
				.getProjects()//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	@Override
	public List<Project> getProjects(Long userId) {
		UserTable user = userDao.findById(userId);
		return projectDao//
				.getProjectsForUser(user)//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	@Override
	public Project getProject(Long projectId) {
		ProjectTable project = projectDao.findById(projectId);
		if (project == null) {
			throw new NotFoundException("Project not found");
		}
		return map(project);
	}

	@Override
	public Project getProject(Long userId, Long projectId) {
		UserTable user = userDao.findById(userId);
		ProjectTable project = projectDao.findById(projectId);
		if (project == null) {
			throw new NotFoundException("Project not found");
		}

		ProjectTable projectForUser = projectDao.findByUserAndId(user, projectId);
		if (projectForUser == null) {
			throw new ForbiddenException("Access forbidden");
		}
		return map(projectForUser);
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