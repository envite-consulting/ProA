package de.envite.proa.usecases.project;

import de.envite.proa.entities.project.AccessDeniedException;
import de.envite.proa.entities.project.NoResultException;
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

	void removeVersion(Long projectId, Long versionId) throws NoResultException;

	void removeVersion(Long userId, Long projectId, Long versionId) throws AccessDeniedException, NoResultException;

	void addContributor(Long userId, Long projectId, String email) throws AccessDeniedException, NoResultException;

	void removeContributor(Long userId, Long projectId, Long contributorId) throws AccessDeniedException, NoResultException;
}
