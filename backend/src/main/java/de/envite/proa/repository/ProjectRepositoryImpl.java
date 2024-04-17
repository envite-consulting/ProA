package de.envite.proa.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.entities.Project;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.usecases.project.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProjectRepositoryImpl implements ProjectRepository {

	private ProjectDao dao;

	@Inject
	public ProjectRepositoryImpl(ProjectDao dao) {
		this.dao = dao;
	}

	@Transactional
	@Override
	public Project createProject(String name) {

		ProjectTable table = new ProjectTable();
		table.setName(name);
		table.setCreatedAt(LocalDateTime.now());
		table.setModifiedAt(LocalDateTime.now());

		dao.persist(table);

		return map(table);
	}

	@Transactional
	@Override
	public List<Project> getProjects() {
		return dao//
				.getProjects()//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}
	
	@Override
	public Project getProject(Long projectId) {
		return map(dao.findById(projectId));
	}

	private Project map(ProjectTable table) {

		Project project = new Project();
		project.setId(table.getId());
		project.setName(table.getName());
		project.setCreatedAt(table.getCreatedAt());
		project.setModifiedAt(table.getModifiedAt());

		return project;
	}
}