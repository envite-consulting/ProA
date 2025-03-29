package de.envite.proa.usecases.project;

import de.envite.proa.entities.project.Project;
import de.envite.proa.entities.project.ProjectVersion;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class ProjectUsecase {

	@Inject
	private ProjectRepository repository;

	public Project createProject(String name, String version) {
		return repository.createProject(name, version);
	}

	public Project createProject(Long userId, String name, String version) {
		return repository.createProject(userId, name, version);
	}

	public ProjectVersion addVersion(Long userId, Long projectId, String versionName) {
		return repository.addVersion(userId, projectId, versionName);
	}

	public ProjectVersion addVersion(Long projectId, String versionName) {
		return repository.addVersion(projectId, versionName);
	}

	public List<Project> getProjects() {
		return repository.getProjects();
	}

	public List<Project> getProjects(Long userId) {
		return repository.getProjects(userId);
	}

	public Project getProject(Long projectId) {
		return repository.getProject(projectId);
	}

	public Project getProject(Long userId, Long projectId) {
		return repository.getProject(userId, projectId);
	}

	public void removeVersion(Long projectId, Long versionId) {
		repository.removeVersion(projectId, versionId);
	}

	public void removeVersion(Long userId, Long projectId, Long versionId) {
		repository.removeVersion(userId, projectId, versionId);
	}

	public void addContributor(Long userId, Long projectId, String email) {
		repository.addContributor(userId, projectId, email);
	}

	public void removeContributor(Long userId, Long projectId, Long contributorId) {
		repository.removeContributor(userId, projectId, contributorId);
	}
}
