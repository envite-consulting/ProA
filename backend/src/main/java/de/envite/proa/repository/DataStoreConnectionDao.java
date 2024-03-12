package de.envite.proa.repository;

import java.util.List;

import de.envite.proa.repository.tables.DataStoreConnectionTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DataStoreConnectionDao {
	
	private EntityManager em;

	@Inject
	public DataStoreConnectionDao(EntityManager em) {
		this.em = em;
	}
	
	@Transactional
	public void persist(DataStoreConnectionTable table) {
		em.persist(table);
	}

	@Transactional
	public List<DataStoreConnectionTable> getDataStoreConnections() {
		return em//
				.createQuery("SELECT dc FROM DataStoreConnectionTable dc", DataStoreConnectionTable.class)//
				.getResultList();
	}
}
