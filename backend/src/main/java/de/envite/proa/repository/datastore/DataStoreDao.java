package de.envite.proa.repository.datastore;

import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProjectTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

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
	public List<DataStoreTable> getDataStores(ProjectTable projectTable) {
		return em //
				.createQuery("SELECT d FROM DataStoreTable d WHERE d.project = :project", DataStoreTable.class)//
				.setParameter("project", projectTable)//
				.getResultList();
	}
}
