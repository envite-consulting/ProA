package de.envite.proa.usecases.project;

import de.envite.proa.entities.project.Project;
import de.envite.proa.entities.project.ProjectVersion;

import java.util.List;

public interface ProjectRepository {

	Project createProject(String name, String version);

	Project createProject(Long userId, String name, String versionName);

	ProjectVersion addVersion(Long userId, Long projectId, String versionName);

	ProjectVersion addVersion(Long projectId, String version);

	List<Project> getProjects();

	List<Project> getProjects(Long userId);

	Project getProject(Long projectId);

	Project getProject(Long userId, Long projectId);

	void removeVersion(Long projectId, Long versionId);

	void removeVersion(Long userId, Long projectId, Long versionId);

	void addContributor(Long userId, Long projectId, String email);

	void removeContributor(Long userId, Long projectId, Long contributorId);
}
