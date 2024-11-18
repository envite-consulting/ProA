package de.envite.proa.usecases.project;

import java.util.List;

import de.envite.proa.entities.Project;

public interface ProjectRepository {

	Project createProject(String name, String version);

	Project createProject(Long userId, String name, String version);

	List<Project> getProjects();

	List<Project> getProjects(Long userId);

	Project getProject(Long projectId);

	Project getProject(Long userId, Long projectId);

	void deleteProject(Long projectId);

	void deleteProject(Long userId, Long projectId);
}
