package de.envite.proa.repository;

import de.envite.proa.repository.datastore.DataStoreDao;
import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProjectTable;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class DataStoreDaoTest {

	private static final String DATA_STORE_LABEL_1 = "Store 1";
	private static final String DATA_STORE_LABEL_2 = "Store 2";

	@Inject
	EntityManager em;

	@Inject
	DataStoreDao dataStoreDao;

	@AfterEach
	@Transactional
	void cleanupDatabase() {
		em.createQuery("DELETE FROM DataStoreTable").executeUpdate();
		em.createQuery("DELETE FROM ProjectTable").executeUpdate();
	}

	@Test
	@Transactional
	void testPersist() {
		ProjectTable projectTable = new ProjectTable();
		em.persist(projectTable);

		DataStoreTable dataStoreTable = new DataStoreTable();
		dataStoreTable.setLabel(DATA_STORE_LABEL_1);
		dataStoreTable.setProject(projectTable);

		dataStoreDao.persist(dataStoreTable);

		flushAndClear();

		DataStoreTable retrieved = em.find(DataStoreTable.class, dataStoreTable.getId());
		assertNotNull(retrieved);
		assertEquals(DATA_STORE_LABEL_1, retrieved.getLabel());
	}

	@Test
	@Transactional
	void testGetDataStores() {
		ProjectTable projectTable = new ProjectTable();
		em.persist(projectTable);

		DataStoreTable dataStore1 = new DataStoreTable();
		dataStore1.setLabel(DATA_STORE_LABEL_1);
		dataStore1.setProject(projectTable);
		em.persist(dataStore1);

		DataStoreTable dataStore2 = new DataStoreTable();
		dataStore2.setLabel(DATA_STORE_LABEL_2);
		dataStore2.setProject(projectTable);
		em.persist(dataStore2);

		List<DataStoreTable> result = dataStoreDao.getDataStores(projectTable);
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(DATA_STORE_LABEL_1, result.get(0).getLabel());
		assertEquals(DATA_STORE_LABEL_2, result.get(1).getLabel());
	}

	private void flushAndClear() {
		em.flush();
		em.clear();
	}
}
