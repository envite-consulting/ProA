package de.envite.proa.repository;

import de.envite.proa.repository.datastore.DataStoreConnectionDao;
import de.envite.proa.repository.tables.DataStoreConnectionTable;
import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class DataStoreConnectionDaoTest {

	@Inject
	DataStoreConnectionDao dataStoreConnectionDao;

	@Inject
	EntityManager em;

	@AfterEach
	@Transactional
	void cleanupDatabase() {
		em.createQuery("DELETE FROM DataStoreConnectionTable").executeUpdate();
		em.createQuery("DELETE FROM DataStoreTable").executeUpdate();
		em.createQuery("DELETE FROM ProcessModelTable").executeUpdate();
		em.createQuery("DELETE FROM ProjectVersionTable").executeUpdate();
	}

	@Test
	@Transactional
	void testPersist() {
		ProjectVersionTable project = new ProjectVersionTable();
		em.persist(project);

		DataStoreTable dataStore = new DataStoreTable();
		dataStore.setProject(project);
		em.persist(dataStore);

		ProcessModelTable processModel = new ProcessModelTable();
		processModel.setProject(project);
		em.persist(processModel);

		DataStoreConnectionTable connection = new DataStoreConnectionTable();
		connection.setProject(project);
		connection.setDataStore(dataStore);
		connection.setProcess(processModel);

		dataStoreConnectionDao.persist(connection);

		flushAndClear();

		DataStoreConnectionTable retrieved = em.find(DataStoreConnectionTable.class, connection.getId());
		assertNotNull(retrieved);
		assertEquals(dataStore.getId(), retrieved.getDataStore().getId());
		assertEquals(processModel.getId(), retrieved.getProcess().getId());
	}

	@Test
	@Transactional
	void testGetDataStoreConnections() {
		ProjectVersionTable project = new ProjectVersionTable();
		em.persist(project);

		DataStoreConnectionTable connection = new DataStoreConnectionTable();
		connection.setProject(project);
		em.persist(connection);

		List<DataStoreConnectionTable> connections = dataStoreConnectionDao.getDataStoreConnections(project);
		assertNotNull(connections);
		assertEquals(1, connections.size());
		assertEquals(project.getId(), connections.get(0).getProject().getId());
	}

	@Test
	@Transactional
	void testDeleteForProcessModel() {
		ProjectVersionTable project = new ProjectVersionTable();
		em.persist(project);

		DataStoreTable dataStore = new DataStoreTable();
		dataStore.setProject(project);
		em.persist(dataStore);

		ProcessModelTable processModel = new ProcessModelTable();
		processModel.setProject(project);
		em.persist(processModel);

		DataStoreConnectionTable connection = new DataStoreConnectionTable();
		connection.setProject(project);
		connection.setDataStore(dataStore);
		connection.setProcess(processModel);
		em.persist(connection);

		dataStoreConnectionDao.deleteForProcessModel(processModel.getId());

		flushAndClear();

		List<DataStoreConnectionTable> connections = dataStoreConnectionDao.getDataStoreConnections(project);
		assertTrue(connections.isEmpty());

		DataStoreTable deletedDataStore = em.find(DataStoreTable.class, dataStore.getId());
		assertNull(deletedDataStore);
	}

	@Test
	@Transactional
	void testDeleteForProcessModel_DontDeleteDataStoreWhenConnectionExists() {
		ProjectVersionTable project = new ProjectVersionTable();
		em.persist(project);

		DataStoreTable dataStore = new DataStoreTable();
		dataStore.setProject(project);
		em.persist(dataStore);

		ProcessModelTable processModel1 = new ProcessModelTable();
		processModel1.setProject(project);
		em.persist(processModel1);

		ProcessModelTable processModel2 = new ProcessModelTable();
		processModel2.setProject(project);
		em.persist(processModel2);

		DataStoreConnectionTable connection1 = new DataStoreConnectionTable();
		connection1.setProject(project);
		connection1.setDataStore(dataStore);
		connection1.setProcess(processModel1);
		em.persist(connection1);

		DataStoreConnectionTable connection2 = new DataStoreConnectionTable();
		connection2.setProject(project);
		connection2.setDataStore(dataStore);
		connection2.setProcess(processModel2);
		em.persist(connection2);

		dataStoreConnectionDao.deleteForProcessModel(processModel1.getId());

		flushAndClear();

		List<DataStoreConnectionTable> connections = dataStoreConnectionDao.getDataStoreConnections(project);
		assertTrue(connections.size() == 1);

		DataStoreTable retrievedDataStore = em.find(DataStoreTable.class, dataStore.getId());
		assertNotNull(retrievedDataStore);
	}

	@Test
	@Transactional
	void testDeleteConnection() {
		ProjectVersionTable project = new ProjectVersionTable();
		em.persist(project);

		DataStoreTable dataStore = new DataStoreTable();
		dataStore.setProject(project);
		em.persist(dataStore);

		ProcessModelTable processModel = new ProcessModelTable();
		processModel.setProject(project);
		em.persist(processModel);

		DataStoreConnectionTable connection = new DataStoreConnectionTable();
		connection.setProject(project);
		connection.setDataStore(dataStore);
		connection.setProcess(processModel);
		em.persist(connection);

		dataStoreConnectionDao.deleteConnection(connection.getId());

		flushAndClear();

		DataStoreConnectionTable deletedConnection = em.find(DataStoreConnectionTable.class, connection.getId());
		assertNull(deletedConnection);
	}

	private void flushAndClear() {
		em.flush();
		em.clear();
	}
}
