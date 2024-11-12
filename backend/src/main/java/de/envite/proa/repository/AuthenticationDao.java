package de.envite.proa.repository;

import de.envite.proa.repository.tables.UserTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthenticationDao {

	@Inject
	private EntityManager em;

	@Transactional
	public void register(UserTable user) {
		em.persist(user);
		em.flush();
	}
}
