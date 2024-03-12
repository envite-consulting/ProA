package de.envite.proa.repository;

import java.util.List;

import de.envite.proa.repository.tables.ProcessConnectionTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProcessConnectionDao {

	private EntityManager em;

	@Inject
	public ProcessConnectionDao(EntityManager em) {
		this.em = em;
	}
	
	@Transactional
	public void persist(ProcessConnectionTable table) {
		em.persist(table);
	}

	@Transactional
	public List<ProcessConnectionTable> getProcessConnections() {
		return em//
				.createQuery("SELECT pc FROM ProcessConnectionTable pc", ProcessConnectionTable.class)//
				.getResultList();
	}
}
