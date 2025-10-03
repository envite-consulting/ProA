package de.envite.proa.repository.datastore;

import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import de.envite.proa.util.SearchLabelBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@ApplicationScoped
public class DataStoreDao {

    @ConfigProperty(name = "quarkus.datasource.db-kind")
    String dbKind;

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
	public List<DataStoreTable> getDataStores(ProjectVersionTable projectVersionTable) {
		return em //
				.createQuery("SELECT d FROM DataStoreTable d WHERE d.project = :project", DataStoreTable.class)//
				.setParameter("project", projectVersionTable)//
				.getResultList();
	}

	@Transactional
	public DataStoreTable getDataStoreForLabel(String label, ProjectVersionTable projectVersionTable) {

		String searchLabel = SearchLabelBuilder.buildSearchLabel(label);

		if ("postgresql".equals(dbKind)) {
			return em.createQuery(
							 "SELECT d FROM DataStoreTable d " +
								"WHERE d.project = :project " +
								"AND ( d.searchLabel = :searchLabel " +
								"OR function('levenshtein', d.searchLabel, :searchLabel) <= 4 )",
							DataStoreTable.class)
					.setParameter("searchLabel", searchLabel)//
					.setParameter("project", projectVersionTable)//
					.getSingleResult();
		} else {
			return em.createQuery(
							 "SELECT d FROM DataStoreTable d " +
								"WHERE d.project = :project " +
								"AND d.searchLabel = :searchLabel ",
							DataStoreTable.class)
					.setParameter("searchLabel", searchLabel)//
					.setParameter("project", projectVersionTable)//
					.getSingleResult();
		}
	}
}
