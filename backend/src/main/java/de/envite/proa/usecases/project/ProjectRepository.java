package de.envite.proa.usecases.project;

import de.envite.proa.entities.Project;

import java.util.List;

public interface ProjectRepository {

    Project createProject(String name, String version);

    List<Project> getProjects();

    Project getProject(Long projectId);
}