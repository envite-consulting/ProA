package de.envite.proa.repository.project;

import de.envite.proa.entities.project.Project;
import de.envite.proa.entities.project.ProjectVersion;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.repository.user.UserDao;
import de.envite.proa.repository.user.UserMapper;
import de.envite.proa.usecases.project.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
		return map(createProjectTable(name, version));
	}

	@Override
	public Project createProject(Long userId, String name, String version) {
		ProjectTable project = createProjectTable(name, version);

		UserTable user = userDao.findById(userId);
		project.setOwner(user);

		projectDao.merge(project);
		return map(project);
	}

	private ProjectTable createProjectTable(String name, String version) {
		LocalDateTime now = LocalDateTime.now();

		ProjectVersionTable projectVersion = new ProjectVersionTable();
		projectVersion.setName(version);
		projectVersion.setCreatedAt(now);
		projectVersion.setModifiedAt(now);
		projectDao.persist(projectVersion);

		ProjectTable project = new ProjectTable();
		project.setName(name);
		project.setCreatedAt(now);
		project.setModifiedAt(now);
		project.getVersions().add(projectVersion);
		projectDao.persist(project);

		return project;
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
		UserTable user = new UserTable();
		user.setId(userId);
		
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
		ProjectTable project = projectDao.findById(projectId);
		List<Long> contributorIds = project.getContributors().stream().map(UserTable::getId).toList();
		if (!Objects.equals(project.getOwner().getId(), userId) && !contributorIds.contains(userId)) {
			throw new ForbiddenException("Not found or Access forbidden");
		}
		return map(project);
	}

	@Override
	public void deleteProjectVersion(Long id) {
		projectDao.deleteProjectVersion(id);
	}

	@Override
	public void deleteProjectVersion(Long userId, Long id) {
		ProjectVersionTable projectVersion = projectDao.findVersionById(id);
		ProjectTable project = projectVersion.getProject();
		Long ownerId = project.getOwner().getId();
		List<Long> contributorIds = project.getContributors().stream().map(UserTable::getId).toList();
		if (!(Objects.equals(ownerId, userId)) && !contributorIds.contains(userId)) {
			throw new ForbiddenException("Not found or Access forbidden");
		}
		projectDao.deleteProjectVersion(id);
	}

	private ProjectVersion map(ProjectVersionTable table) {
		ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setId(table.getId());
		projectVersion.setName(table.getName());
		projectVersion.setCreatedAt(table.getCreatedAt());
		projectVersion.setModifiedAt(table.getModifiedAt());
		return projectVersion;
	}

	private Project map(ProjectTable table) {
		Project project = new Project();
		project.setId(table.getId());
		project.setName(table.getName());
		project.setCreatedAt(table.getCreatedAt());
		project.setModifiedAt(table.getModifiedAt());

		project.setVersions(table.getVersions().stream().map(this::map).collect(Collectors.toSet()));
		project.setContributors(table.getContributors().stream().map(UserMapper::map).collect(Collectors.toSet()));
		project.setOwner(UserMapper.map(table.getOwner()));

		return project;
	}
}
