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

	private EntityManager em;

	@Inject
	public ProjectRepositoryImpl(EntityManager em) {
		this.em = em;
	}

	@Transactional
	@Override
	public Project createProject(String name) {

		ProjectTable table = new ProjectTable();
		table.setName(name);
		table.setCreatedAt(LocalDateTime.now());
		table.setModifiedAt(LocalDateTime.now());

		em.persist(table);

		return map(table);
	}

	@Transactional
	@Override
	public List<Project> getProjects() {
		return em//
				.createQuery("SELECT p FROM ProjectTable p", ProjectTable.class)//
				.getResultList()//
				.stream()//
				.map(this::map)//
				.collect(Collectors.toList());
	}

	private Project map(ProjectTable table) {

		Project project = new Project();
		project.setName(table.getName());
		project.setCreatedAt(table.getCreatedAt());
		project.setModifiedAt(table.getModifiedAt());

		return project;
	}
}