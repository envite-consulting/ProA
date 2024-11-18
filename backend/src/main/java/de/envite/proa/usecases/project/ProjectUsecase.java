package de.envite.proa.usecases.project;

import java.util.List;

import de.envite.proa.entities.Project;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

	public void deleteProject(Long projectId) {
		repository.deleteProject(projectId);
	}

	public void deleteProject(Long userId, Long projectId) {
		repository.deleteProject(userId, projectId);
	}
}
