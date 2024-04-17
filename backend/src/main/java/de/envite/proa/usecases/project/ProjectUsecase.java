package de.envite.proa.usecases.project;

import java.util.List;

import de.envite.proa.entities.Project;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProjectUsecase {
	
	@Inject
	private ProjectRepository repository;
	
	public Project createProject(String name) {
		return repository.createProject(name);
	}
	
	public List<Project> getProjects(){
		return repository.getProjects();
	}

	public Project getProject(Long projectId) {
		return repository.getProject(projectId);
	}
}