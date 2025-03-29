package de.envite.proa.repository.project;

import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import de.envite.proa.repository.tables.UserTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ProjectDao {

	private EntityManager em;

	@Inject
	public ProjectDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public void persist(ProjectTable table) {
		em.persist(table);
	}

	@Transactional
	public void persist(ProjectVersionTable table) {
		em.persist(table);
	}

	@Transactional
	public void merge(ProjectTable table) {
		em.merge(table);
	}

	@Transactional
	public List<ProjectTable> getProjectsWithVersionsAndContributors() {
		return em//
				.createQuery("SELECT p FROM ProjectTable p", ProjectTable.class)//
				.getResultList();
	}

	@Transactional
	public List<ProjectTable> getAllProjectsForUserWithVersionsAndContributors(UserTable user) {
		EntityGraph<?> graph = em.getEntityGraph("Project.withVersionsAndContributors");

		return em.createQuery("SELECT DISTINCT p " +
						"FROM ProjectTable p " +
						"LEFT JOIN p.contributors c " +
						"WHERE c = :user " +
						"OR p.owner = :user", ProjectTable.class)
				.setParameter("user", user)
				.setHint("jakarta.persistence.loadgraph", graph)
				.getResultList();
	}

	@Transactional
	public ProjectTable findByIdWithVersions(Long id) {
		EntityGraph<?> graph = em.getEntityGraph("Project.withVersions");

		return em.find(ProjectTable.class, id,
				java.util.Collections.singletonMap("jakarta.persistence.loadgraph", graph)
		);
	}

	@Transactional
	public ProjectTable findByIdWithContributors(Long id) {
		EntityGraph<?> graph = em.getEntityGraph("Project.withContributors");

		return em.find(ProjectTable.class, id,
				java.util.Collections.singletonMap("jakarta.persistence.loadgraph", graph)
		);
	}

	@Transactional
	public ProjectTable findByIdWithVersionsAndContributors(Long id) {
		EntityGraph<?> graph = em.getEntityGraph("Project.withVersionsAndContributors");

		return em.find(ProjectTable.class, id,
				java.util.Collections.singletonMap("jakarta.persistence.loadgraph", graph)
		);
	}

	@Transactional
	public ProjectVersionTable findVersionById(Long id) {
		return em.find(ProjectVersionTable.class, id);
	}

	@Transactional
	public void deleteById(Long id) {
		ProjectTable table = em.find(ProjectTable.class, id);
		em.remove(table);
	}
}
