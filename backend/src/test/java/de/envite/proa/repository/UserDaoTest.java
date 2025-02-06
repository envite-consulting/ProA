package de.envite.proa.repository;

import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.repository.user.UserDao;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserDaoTest {

	private static final String EMAIL_1 = "test1@example.com";
	private static final String EMAIL_2 = "test2@example.com";

	@Inject
	UserDao userDao;

	@Inject
	EntityManager em;

	@BeforeEach
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

	@Test
	@Transactional
	void testDeleteById() {
		UserTable user = new UserTable();
		user.setEmail(EMAIL_1);
		em.persist(user);

		userDao.deleteById(user.getId());

		flushAndClear();

		UserTable deletedUser = em.find(UserTable.class, user.getId());
		assertNull(deletedUser);
	}

	@Test
	@Transactional
	void testGetAllUsers() {
		UserTable user1 = new UserTable();
		user1.setEmail(EMAIL_1);
		em.persist(user1);

		UserTable user2 = new UserTable();
		user2.setEmail(EMAIL_2);
		em.persist(user2);

		List<UserTable> users = userDao.getAllUsers();

		assertEquals(2, users.size());
		assertEquals(user1, users.get(0));
		assertEquals(user2, users.get(1));
	}

	private void flushAndClear() {
		em.flush();
		em.clear();
	}
}
