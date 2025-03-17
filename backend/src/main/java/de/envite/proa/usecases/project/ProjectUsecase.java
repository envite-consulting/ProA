package de.envite.proa.usecases.project;

import de.envite.proa.entities.project.Project;
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

	public void deleteProjectVersion(Long projectVersionId) {
		repository.deleteProjectVersion(projectVersionId);
	}

	public void deleteProjectVersion(Long userId, Long projectVersionId) {
		repository.deleteProjectVersion(userId, projectVersionId);
	}
}
