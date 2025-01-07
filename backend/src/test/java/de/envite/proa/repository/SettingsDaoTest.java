package de.envite.proa.repository;

import de.envite.proa.repository.tables.SettingsTable;
import de.envite.proa.repository.tables.UserTable;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SettingsDaoTest {

    private static final String EMAIL = "example@email.com";
    private static final String API_KEY = "api-key";

    @Inject
    EntityManager em;

    @Inject
    SettingsDao settingsDao;

    private UserTable user;
    private SettingsTable settings;

    @BeforeEach
    @Transactional
    void setUp() {
        user = new UserTable();
        user.setEmail(EMAIL);
        em.persist(user);

        settings = new SettingsTable();
        settings.setUser(user);
        settingsDao.persist(settings);
    }

    @AfterEach
    @Transactional
    void cleanup() {
        em.createQuery("DELETE FROM SettingsTable").executeUpdate();
        em.createQuery("DELETE FROM UserTable").executeUpdate();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    @Transactional
    void testGetSettings() {
        SettingsTable savedSettings = settingsDao.getSettings();
        assertNotNull(savedSettings);
        assertEquals(settings.getId(), savedSettings.getId());
    }

    @Test
    @Transactional
    void testGetSettingsNotFound() {
        em.createQuery("DELETE FROM SettingsTable").executeUpdate();

        flushAndClear();

        SettingsTable savedSettings = settingsDao.getSettings();
        assertNull(savedSettings);
    }

    @Test
    @Transactional
    void testGetSettingsForUser() {
        SettingsTable savedSettings = settingsDao.getSettingsForUser(user);
        assertNotNull(savedSettings);
        assertEquals(settings.getId(), savedSettings.getId());
    }

    @Test
    @Transactional
    void testGetSettingsForUserNotFound() {
        em.createQuery("DELETE FROM SettingsTable").executeUpdate();

        flushAndClear();

        SettingsTable savedSettings = settingsDao.getSettingsForUser(user);
        assertNull(savedSettings);
    }

    @Test
    @Transactional
    void testPersistSettings() {
        SettingsTable persistedSettings = em.find(SettingsTable.class, settings.getId());

        assertNotNull(persistedSettings.getId());
        assertEquals(settings.getId(), persistedSettings.getId());
    }

    @Test
    @Transactional
    void testMergeSettings() {
        settings.setGeminiApiKey(API_KEY);
        settingsDao.merge(settings);

        flushAndClear();

        SettingsTable retrievedSettings = em.find(SettingsTable.class, settings.getId());
        assertEquals(API_KEY, retrievedSettings.getGeminiApiKey());
    }
}
