package de.envite.proa.repository;

import de.envite.proa.repository.tables.SettingsTable;
import de.envite.proa.repository.tables.UserTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
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
		try {
			return em.createQuery("SELECT s FROM SettingsTable s", SettingsTable.class).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Transactional
	public SettingsTable getSettingsForUser(UserTable user) {
		try {
			return em //
					.createQuery("SELECT s FROM SettingsTable s WHERE s.user = :user", SettingsTable.class) //
					.setParameter("user", user) //
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
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