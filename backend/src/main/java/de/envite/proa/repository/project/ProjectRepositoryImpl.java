package de.envite.proa.repository.project;

import de.envite.proa.entities.authentication.User;
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
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
		ProjectTable project = createProjectTable(name, version);
		return map(project,
				project.getVersions().stream().map(this::map).collect(Collectors.toSet()),
				project.getContributors().stream().map(UserMapper::map).collect(Collectors.toSet()));
	}

	@Override
	public Project createProject(Long userId, String name, String version) {
		ProjectTable project = createProjectTable(name, version);

		UserTable user = userDao.findById(userId);
		project.setOwner(user);

		projectDao.merge(project);
		return map(project,
				project.getVersions().stream().map(this::map).collect(Collectors.toSet()),
				project.getContributors().stream().map(UserMapper::map).collect(Collectors.toSet()));
	}

	private ProjectTable createProjectTable(String name, String version) {
		LocalDateTime now = LocalDateTime.now();

		ProjectVersionTable projectVersion = new ProjectVersionTable();
		projectVersion.setName(version);
		projectVersion.setCreatedAt(now);
		projectVersion.setModifiedAt(now);

		ProjectTable project = new ProjectTable();
		project.setName(name);
		project.setCreatedAt(now);
		project.setModifiedAt(now);
		project.getVersions().add(projectVersion);
		projectDao.persist(project);

		return project;
	}

	@Override
	public ProjectVersion addVersion(Long userId, Long projectId, String versionName) {
		ProjectTable project = projectDao.findByIdWithVersionsAndContributors(projectId);
		if (project == null || !Objects.equals(project.getOwner().getId(), userId)) {
			throw new ForbiddenException("Not found or Access forbidden");
		}

		return createVersionAndMergeProject(project, versionName);
	}

	@Override
	public ProjectVersion addVersion(Long projectId, String versionName) {
		ProjectTable project = projectDao.findByIdWithVersionsAndContributors(projectId);
		return createVersionAndMergeProject(project, versionName);
	}

	private ProjectVersion createVersionAndMergeProject(ProjectTable project, String versionName) {
		ProjectVersionTable projectVersion = new ProjectVersionTable();
		projectVersion.setName(versionName);

		LocalDateTime now = LocalDateTime.now();
		projectVersion.setCreatedAt(now);
		projectVersion.setModifiedAt(now);

		projectDao.persist(projectVersion);

		project.getVersions().add(projectVersion);
		project.setModifiedAt(now);
		projectDao.merge(project);
		return map(projectVersion);
	}

	@Override
	public List<Project> getProjects() {
		return projectDao//
				.getProjectsWithVersionsAndContributors()//
				.stream()//
				.map(project -> map(//
						project,//
						project.getVersions().stream().map(this::map).collect(Collectors.toSet()),//
						project.getContributors().stream().map(UserMapper::map).collect(Collectors.toSet())))//
				.collect(Collectors.toList());
	}

	@Override
	public List<Project> getProjects(Long userId) {
		UserTable user = userDao.findById(userId);

		return projectDao//
				.getAllProjectsForUserWithVersionsAndContributors(user)//
				.stream()//
				.map(project -> map(//
						project,//
						project.getVersions().stream().map(this::map).collect(Collectors.toSet()),//
						project.getContributors().stream().map(UserMapper::map).collect(Collectors.toSet())))//
				.collect(Collectors.toList());
	}

	@Override
	public Project getProject(Long projectId) {
		ProjectTable project = projectDao.findByIdWithVersionsAndContributors(projectId);
		if (project == null) {
			throw new NotFoundException("Project not found");
		}
		return map(project, map(project.getVersions()), UserMapper.map(project.getContributors()));
	}

	@Override
	public Project getProject(Long userId, Long projectId) {
		ProjectTable project = projectDao.findByIdWithVersionsAndContributors(projectId);
		List<Long> contributorIds = project.getContributors().stream().map(UserTable::getId).toList();
		if (!Objects.equals(project.getOwner().getId(), userId) && !contributorIds.contains(userId)) {
			throw new ForbiddenException("Not found or Access forbidden");
		}
		return map(project, map(project.getVersions()), UserMapper.map(project.getContributors()));
	}

	@Override
	public void removeVersion(Long projectId, Long versionId) {
		ProjectTable project = findProjectWithVersionsOrThrow(projectId);
		if (isSingleVersion(project, versionId)) {
			deleteEntireProject(projectId);
		} else {
			removeVersionFromProject(project, versionId);
		}
	}

	@Override
	public void removeVersion(Long userId, Long projectId, Long versionId) {
		ProjectTable project = findProjectWithVersionsOrThrow(projectId);
		validateProjectOwner(project, userId);

		if (isSingleVersion(project, versionId)) {
			deleteEntireProject(projectId);
		} else {
			removeVersionFromProject(project, versionId);
		}
	}

	@Override
	public void addContributor(Long userId, Long projectId, String email) {
		ProjectTable project = findProjectWithContributorsOrThrow(projectId);
		validateProjectOwner(project, userId);
		UserTable user = userDao.findByEmail(email);
		if (user == null) {
			throw new EntityNotFoundException("User not found with email: " + email);
		}
		project.getContributors().add(user);
		project.setModifiedAt(LocalDateTime.now());
		projectDao.merge(project);
	}

	@Override
	public void removeContributor(Long userId, Long projectId, Long contributorId) {
		ProjectTable project = findProjectWithContributorsOrThrow(projectId);
		validateProjectOwner(project, userId);
		project.getContributors().removeIf(user -> Objects.equals(user.getId(), contributorId));
		project.setModifiedAt(LocalDateTime.now());
		projectDao.merge(project);
	}

	private ProjectTable findProjectWithContributorsOrThrow(Long projectId) {
		return Optional.ofNullable(projectDao.findByIdWithContributors(projectId))
				.orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId));
	}

	private ProjectTable findProjectWithVersionsOrThrow(Long projectId) {
		return Optional.ofNullable(projectDao.findByIdWithVersions(projectId))
				.orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId));
	}

	private void validateProjectOwner(ProjectTable project, Long userId) {
		if (!Objects.equals(project.getOwner().getId(), userId)) {
			throw new ForbiddenException("User does not have permission to modify this project.");
		}
	}

	private boolean isSingleVersion(ProjectTable project, Long versionId) {
		return project.getVersions().size() == 1 &&
				Objects.equals(project.getVersions().iterator().next().getId(), versionId);
	}

	private void deleteEntireProject(Long projectId) {
		projectDao.deleteById(projectId);
	}

	private void removeVersionFromProject(ProjectTable project, Long versionId) {
		boolean removed = project.getVersions()
				.removeIf(version -> Objects.equals(version.getId(), versionId));
		if (!removed) {
			throw new EntityNotFoundException("Version not found with ID: " + versionId);
		}
		project.setModifiedAt(LocalDateTime.now());

		projectDao.merge(project);
	}

	private Set<ProjectVersion> map(Set<ProjectVersionTable> versions) {
		return versions.stream().map(this::map).collect(Collectors.toSet());
	}

	private ProjectVersion map(ProjectVersionTable table) {
		ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setId(table.getId());
		projectVersion.setName(table.getName());
		projectVersion.setCreatedAt(table.getCreatedAt());
		projectVersion.setModifiedAt(table.getModifiedAt());
		return projectVersion;
	}

	private Project map(ProjectTable table, Set<ProjectVersion> versions, Set<User> contributors) {
		Project project = new Project();
		project.setId(table.getId());
		project.setName(table.getName());
		project.setCreatedAt(table.getCreatedAt());
		project.setModifiedAt(table.getModifiedAt());

		project.getVersions().addAll(versions);
		project.getContributors().addAll(contributors);
		project.setOwner(UserMapper.map(table.getOwner()));

		return project;
	}
}
