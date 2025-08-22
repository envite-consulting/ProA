package de.envite.proa.repository.project;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.envite.proa.entities.authentication.User;
import de.envite.proa.entities.project.AccessDeniedException;
import de.envite.proa.entities.project.NoResultException;
import de.envite.proa.entities.project.Project;
import de.envite.proa.entities.project.ProjectMember;
import de.envite.proa.entities.project.ProjectRole;
import de.envite.proa.entities.project.ProjectVersion;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.ProjectUserRelationTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.repository.user.UserDao;
import de.envite.proa.usecases.project.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;

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
		
		projectVersion.setProject(project);

		projectDao.persist(project);
		return map(project);
	}

	@Override
	public Project createProject(Long userId, String name, String version) {
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

		projectVersion.setProject(project);

		UserTable user = new UserTable();
		user.setId(userId);
		ProjectUserRelationTable relation = new ProjectUserRelationTable();
		relation.setUser(user);
		relation.setProject(project);
		relation.setRole(ProjectRole.OWNER);
		
		project.getUserRelations().add(relation);
		
		projectDao.persist(project);
		return map(project);
	}

	@Override
	public ProjectVersion addVersion(Long userId, Long projectId, String versionName) {
		ProjectTable project = projectDao.findByIdWithVersionsAndContributors(projectId);
		
		List<Long> owners = project//
			.getUserRelations()//
			.stream()//
			.filter(relation -> relation.getRole().equals(ProjectRole.OWNER))//
			.map(relation -> relation.getUser().getId())
			.toList();
		
		if (project == null || !owners.contains(userId)) {
			throw new AccessDeniedException("Not found or Access forbidden");
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
				.map(project -> map(project))//
				.toList();
	}

	@Override
	public List<Project> getProjects(Long userId) {
		UserTable user = new UserTable();
		user.setId(userId);

		return projectDao//
				.getAllProjectsForUserWithVersionsAndContributors(user)//
				.stream()//
				.map(project -> map(project))//
				.toList();
	}

	@Override
	public Project getProject(Long projectId) {
		ProjectTable project = projectDao.findByIdWithVersionsAndContributors(projectId);
		if (project == null) {
			throw new NoResultException("Project not found");
		}
		return map(project);
	}

	@Override
	public Project getProject(Long userId, Long projectId) {
		ProjectTable project = projectDao.findByIdWithVersionsAndContributors(projectId);
		
		List<Long> allowedUsers = project//
				.getUserRelations()//
				.stream()//
				.map(relation -> relation.getUser().getId())
				.toList();
		
		if (!allowedUsers.contains(userId)) {
			throw new AccessDeniedException("Not found or Access forbidden");
		}
		return map(project);
	}

	@Override
	public void removeVersion(Long projectId, Long versionId) throws NoResultException {
		ProjectTable project = findProjectWithVersionsAndContributorsOrThrow(projectId);
		if (isSingleVersion(project, versionId)) {
			deleteEntireProject(projectId);
		} else {
			removeVersionFromProject(project, versionId);
		}
	}

	@Override
	public void removeVersion(Long userId, Long projectId, Long versionId) throws AccessDeniedException, NoResultException {
		ProjectTable project = findProjectWithVersionsAndContributorsOrThrow(projectId);
		validateProjectOwner(project, userId);

		if (isSingleVersion(project, versionId)) {
			deleteEntireProject(projectId);
		} else {
			removeVersionFromProject(project, versionId);
		}
	}

	@Override
	public void addContributor(Long userId, Long projectId, String email) throws AccessDeniedException, NoResultException {
		ProjectTable project = findProjectWithContributorsOrThrow(projectId);
		validateProjectOwner(project, userId);
		UserTable user = userDao.findByEmail(email);
		if (user == null) {
			throw new EntityNotFoundException("User not found with email: " + email);
		}
		
		ProjectUserRelationTable relation = new ProjectUserRelationTable();
		relation.setProject(project);
		relation.setUser(user);
		relation.setRole(ProjectRole.COLLABORATEUR);
		
		projectDao.persistProjectMember(relation);
	}

	@Override
	public void removeContributor(Long userId, Long projectId, Long contributorId) throws AccessDeniedException, NoResultException {
		ProjectTable project = findProjectWithContributorsOrThrow(projectId);
		validateProjectOwner(project, userId);
		project.getUserRelations().removeIf(relation -> relation.getUser().getId().equals(contributorId));
		project.setModifiedAt(LocalDateTime.now());
		projectDao.merge(project);
	}

	private ProjectTable findProjectWithContributorsOrThrow(Long projectId) throws NoResultException {
		return Optional.ofNullable(projectDao.findByIdWithContributors(projectId))
				.orElseThrow(() -> new NoResultException("Project not found with ID: " + projectId));
	}

	private ProjectTable findProjectWithVersionsAndContributorsOrThrow(Long projectId) throws NoResultException {
		return Optional.ofNullable(projectDao.findByIdWithVersionsAndContributors(projectId))
				.orElseThrow(() -> new NoResultException("Project not found with ID: " + projectId));
	}

	private void validateProjectOwner(ProjectTable project, Long userId) throws AccessDeniedException {
		
		List<Long> owners = project//
				.getUserRelations()//
				.stream()//
				.filter(relation -> relation.getRole().equals(ProjectRole.OWNER))//
				.map(relation -> relation.getUser().getId())
				.toList();
		
		if (!owners.contains(userId)) {
			throw new AccessDeniedException("User does not have permission to modify this project.");
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
			throw new NoResultException("Version not found with ID: " + versionId);
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

	private Project map(ProjectTable table) {
		Project project = new Project();
		project.setId(table.getId());
		project.setName(table.getName());
		project.setCreatedAt(table.getCreatedAt());
		project.setModifiedAt(table.getModifiedAt());

		project.getVersions().addAll(map(table.getVersions()));
		project.getProjectMembers().addAll(mapUsers(table.getUserRelations()));

		return project;
	}

	private Set<ProjectMember> mapUsers(Set<ProjectUserRelationTable> userRelations) {
		
		return userRelations//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toSet());
	}
	
	private ProjectMember map(ProjectUserRelationTable relationTable) {
		ProjectMember member = new ProjectMember();
		member.setFirstName(relationTable.getUser().getFirstName());
		member.setLastName(relationTable.getUser().getLastName());
		member.setRole(relationTable.getRole());
		return member;
	}
}