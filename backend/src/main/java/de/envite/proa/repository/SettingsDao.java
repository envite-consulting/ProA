package de.envite.proa.repository;

import de.envite.proa.repository.tables.SettingsTable;
import de.envite.proa.repository.tables.UserTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SettingsDao {

	private final EntityManager em;

	@Inject
	public SettingsDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public SettingsTable getSettings() {
		TypedQuery<SettingsTable> query = em.createQuery("SELECT s FROM SettingsTable s", SettingsTable.class);
		return query.getSingleResult();
	}

	@Transactional
	public SettingsTable getSettingsForUser(UserTable user) {
		TypedQuery<SettingsTable> query = em.createQuery("SELECT s FROM SettingsTable s WHERE s.user = :user", SettingsTable.class);
		query.setParameter("user", user);
		return query.getSingleResult();
	}

	@Transactional
	public SettingsTable persist(SettingsTable settings) {
		em.persist(settings);
		return settings;
	}

	@Transactional
	public SettingsTable merge(SettingsTable settings) {
		return em.merge(settings);
	}
}