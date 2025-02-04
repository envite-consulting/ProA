package de.envite.proa.repository.project;

import java.util.List;

import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.UserTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

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
	public List<ProjectTable> getProjects() {
		return em//
				.createQuery("SELECT p FROM ProjectTable p", ProjectTable.class)//
				.getResultList();
	}

	@Transactional
	public List<ProjectTable> getProjectsForUser(UserTable user) {
		return em//
				.createQuery("SELECT p FROM ProjectTable p WHERE p.user = :user", ProjectTable.class)//
				.setParameter("user", user)//
				.getResultList();
	}

	@Transactional
	public ProjectTable findById(Long id) {
		return em.find(ProjectTable.class, id);
	}

	@Transactional
	public ProjectTable findByUserAndId(UserTable user, Long id) {
		return em//
				.createQuery("SELECT p FROM ProjectTable p WHERE p.user = :user AND p.id = :id", ProjectTable.class)//
				.setParameter("user", user)//
				.setParameter("id", id)//
				.getResultStream() //
				.findFirst() //
				.orElse(null);
	}
}