package de.envite.proa.usecases.project;

import java.util.List;

import de.envite.proa.entities.Project;

public interface ProjectRepository {

	Project createProject(String name, String version);

	List<Project> getProjects();

	Project getProject(Long projectId);
}