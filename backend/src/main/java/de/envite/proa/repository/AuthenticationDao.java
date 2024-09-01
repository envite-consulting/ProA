package de.envite.proa.repository;

import de.envite.proa.repository.tables.UserTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AuthenticationDao {

	@Inject
	private EntityManager em;

	@Transactional
	public UserTable register(UserTable user) {
		em.persist(user);
		em.flush();
		return user;
	}

	@Transactional
	public UserTable findByEmail(String email) {
		try {
			return em.createQuery("SELECT u FROM UserTable u WHERE u.email = :email", UserTable.class)
					.setParameter("email", email).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	public UserTable findById(Long id) {
		return em.find(UserTable.class, id);
	}

	@Transactional
	public UserTable patchUser(UserTable user) {
		return em.merge(user);
	}
}
