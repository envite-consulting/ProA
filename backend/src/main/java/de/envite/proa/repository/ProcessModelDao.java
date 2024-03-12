package de.envite.proa.repository;

import java.util.List;

import de.envite.proa.repository.tables.ProcessModelTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProcessModelDao {

	private EntityManager em;

	@Inject
	public ProcessModelDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public List<ProcessModelTable> getProcessModels() {
		return em//
				.createQuery("SELECT pm FROM ProcessModelTable pm", ProcessModelTable.class)//
				.getResultList();
	}

	@Transactional
	public List<ProcessModelTable> getProcessModelsForName(String name) {
		return em //
				.createQuery("SELECT p FROM ProcessModelTable p WHERE p.name = :name", ProcessModelTable.class)
				.setParameter("name", name)//
				.getResultList();
	}

	@Transactional
	public void persist(ProcessModelTable table) {
		em.persist(table);
	}

	@Transactional
	public ProcessModelTable find(Long id) {
		return em.find(ProcessModelTable.class, id);
	}
}
