package de.envite.proa.repository;

import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.repository.user.UserDao;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserDaoTest {

	private static final String EMAIL_1 = "test1@example.com";
	private static final String EMAIL_2 = "test2@example.com";

	@Inject
	UserDao userDao;

	@Inject
	EntityManager em;

	@AfterEach
	@Transactional
	void cleanupDatabase() {
		em.createQuery("DELETE FROM UserTable").executeUpdate();
	}

	@Test
	@Transactional
	void testFindByEmail() {
		UserTable user = new UserTable();
		user.setEmail(EMAIL_1);
		em.persist(user);

		UserTable foundUser = userDao.findByEmail(EMAIL_1);

		assertNotNull(foundUser);
		assertEquals(EMAIL_1, foundUser.getEmail());
	}

	@Test
	@Transactional
	void testFindByEmailNotFound() {
		UserTable foundUser = userDao.findByEmail(EMAIL_1);
		assertNull(foundUser);
	}

	@Test
	@Transactional
	void testFindById() {
		UserTable user = new UserTable();
		user.setEmail(EMAIL_1);
		em.persist(user);

		UserTable foundUser = userDao.findById(user.getId());
		assertNotNull(foundUser);
		assertEquals(user.getId(), foundUser.getId());
	}

	@Test
	@Transactional
	void testPatchUser() {
		UserTable user = new UserTable();
		user.setEmail(EMAIL_1);
		em.persist(user);

		user.setEmail(EMAIL_2);

		UserTable updatedUser = userDao.patchUser(user);

		flushAndClear();

		assertNotNull(updatedUser);
		assertEquals(EMAIL_2, updatedUser.getEmail());

		UserTable dbUser = em.find(UserTable.class, user.getId());
		assertEquals(EMAIL_2, dbUser.getEmail());
	}

	private void flushAndClear() {
		em.flush();
		em.clear();
	}
}
