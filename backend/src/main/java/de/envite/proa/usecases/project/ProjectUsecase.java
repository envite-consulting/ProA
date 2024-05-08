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

	public List<Project> getProjects() {
		return repository.getProjects();
	}

	public Project getProject(Long projectId) {
		return repository.getProject(projectId);
	}
}