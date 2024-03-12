package de.envite.proa.repository;

import java.util.List;

import de.envite.proa.repository.tables.DataStoreTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DataStoreDao {

	private EntityManager em;

	@Inject
	public DataStoreDao(EntityManager em) {
		this.em = em;
	}
	
	@Transactional
	public void persist(DataStoreTable dataStoreTable) {
		em.persist(dataStoreTable);
	}

	@Transactional
	public List<DataStoreTable> getDataStores() {
		return em //
				.createQuery("SELECT d FROM DataStoreTable d", DataStoreTable.class)//
				.getResultList();
	}

	@Transactional
	public DataStoreTable getDataStoreForLabel(String label) {
		return em //
				.createQuery("SELECT d FROM DataStoreTable d WHERE d.label = :label", DataStoreTable.class)
				.setParameter("label", label)//
				.getSingleResult();
	}
}
