package de.envite.proa.usecases.project;

import de.envite.proa.entities.project.Project;

import java.util.List;

public interface ProjectRepository {

	Project createProject(String name, String version);

	Project createProject(Long userId, String name, String version);

	List<Project> getProjects();

	List<Project> getProjects(Long userId);

	Project getProject(Long projectId);

	Project getProject(Long userId, Long projectId);

	void deleteProjectVersion(Long id);

	void deleteProjectVersion(Long userId, Long id);
}
