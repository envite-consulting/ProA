package de.envite.proa.repository;

import java.util.List;

import de.envite.proa.repository.tables.ProjectTable;
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
	public ProjectTable findById(Long id) {
		return em.find(ProjectTable.class, id);
	}
}