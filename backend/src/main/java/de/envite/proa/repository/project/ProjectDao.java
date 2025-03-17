package de.envite.proa.repository.project;

import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import de.envite.proa.repository.tables.UserTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
	public List<ProjectTable> getProjects() {
		return em//
				.createQuery("SELECT p FROM ProjectTable p", ProjectTable.class)//
				.getResultList();
	}

	@Transactional
	public List<ProjectTable> getProjectsForUser(UserTable user) {
		return em.createQuery(
						"SELECT DISTINCT p FROM ProjectTable p JOIN p.contributors c WHERE c = :user", ProjectTable.class)
				.setParameter("user", user)
				.getResultList();
	}

	@Transactional
	public ProjectTable findById(Long id) {
		return em.find(ProjectTable.class, id);
	}

	@Transactional
	public ProjectVersionTable findVersionById(Long id) {
		return em.find(ProjectVersionTable.class, id);
	}

	@Transactional
	public void deleteProjectVersion(Long id) {
		ProjectVersionTable table = em.find(ProjectVersionTable.class, id);
		em.remove(table);
	}
}
