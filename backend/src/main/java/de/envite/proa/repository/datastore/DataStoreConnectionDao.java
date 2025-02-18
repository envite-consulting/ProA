package de.envite.proa.repository.datastore;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.proa.repository.tables.DataStoreConnectionTable;
import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
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
	public List<DataStoreConnectionTable> getDataStoreConnections(ProjectTable projectTable) {
		return em//
				.createQuery("SELECT dc FROM DataStoreConnectionTable dc WHERE dc.project = :project", DataStoreConnectionTable.class)//
				.setParameter("project", projectTable)
				.getResultList();
	}

	@Transactional
	public void deleteForProcessModel(Long id) {

		ProcessModelTable processModel = em.find(ProcessModelTable.class, id);

		List<DataStoreTable> dataStores = getDataStoresThatAreConnectedToProcessModel(processModel);

		deleteConnectionsToProcessModel(processModel);

		removeDataStoresThatDontHaveConnections(dataStores);
	}

	@Transactional
	public void deleteConnection(Long connectionId) {
		DataStoreConnectionTable dataStoreConnection = em.find(DataStoreConnectionTable.class, connectionId);
		em.remove(dataStoreConnection);
	}

	private void deleteConnectionsToProcessModel(ProcessModelTable processModel) {
		em.createQuery("DELETE FROM DataStoreConnectionTable dc WHERE dc.process = :processModel")
				.setParameter("processModel", processModel)//
				.executeUpdate();
	}

	private List<DataStoreTable> getDataStoresThatAreConnectedToProcessModel(ProcessModelTable processModel) {
		List<DataStoreTable> dataStores = em
				.createQuery("SELECT dc FROM DataStoreConnectionTable dc WHERE dc.process = :processModel",
						DataStoreConnectionTable.class)
				.setParameter("processModel", processModel)//
				.getResultList().stream()//
				.map(connection -> connection.getDataStore())//
				.collect(Collectors.toList());
		return dataStores;
	}

	private void removeDataStoresThatDontHaveConnections(List<DataStoreTable> dataStores) {
		dataStores.forEach(dataStore -> {

			List<DataStoreConnectionTable> dataStoreConnections = em
					.createQuery("SELECT dc FROM DataStoreConnectionTable dc WHERE dc.dataStore = :dataStore",
							DataStoreConnectionTable.class)
					.setParameter("dataStore", dataStore)//
					.getResultList();

			if (dataStoreConnections.isEmpty()) {
				em.createQuery("DELETE FROM DataStoreTable ds WHERE ds.id = :id")//
						.setParameter("id", dataStore.getId())//
						.executeUpdate();
			}
		});
	}
}