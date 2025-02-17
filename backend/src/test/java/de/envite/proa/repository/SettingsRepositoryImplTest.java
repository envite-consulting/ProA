package de.envite.proa.repository;

import de.envite.proa.entities.settings.Settings;
import de.envite.proa.repository.settings.SettingsDao;
import de.envite.proa.repository.settings.SettingsRepositoryImpl;
import de.envite.proa.repository.tables.SettingsTable;
import de.envite.proa.repository.tables.UserTable;
import de.envite.proa.repository.user.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SettingsRepositoryImplTest {

	@Mock
	private SettingsDao settingsDao;

	@Mock
	private UserDao userDao;

	@InjectMocks
	private SettingsRepositoryImpl settingsRepository;

	private static final Long USER_ID = 1L;
	private static final String GEMINI_API_KEY = "test-api-key";
	private static final String MODELER_CLIENT_ID = "test-modeler-client-id";
	private static final String MODELER_CLIENT_SECRET = "test-modeler-client-secret";
	private static final String OPERATE_CLIENT_ID = "test-operate-client-id";
	private static final String OPERATE_CLIENT_SECRET = "test-operate-client-secret";
	private static final String OPERATE_REGION_ID = "test-operate-region-id";
	private static final String OPERATE_CLUSTER_ID = "test-operate-cluster-id";

	private Settings settings;
	private SettingsTable settingsTable;
	private UserTable userTable;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		settings = new Settings();
		settings.setGeminiApiKey(GEMINI_API_KEY);

		settingsTable = new SettingsTable();
		settingsTable.setGeminiApiKey(GEMINI_API_KEY);

		userTable = new UserTable();
		userTable.setId(USER_ID);
	}

	@Test
	void testGetSettings_Global() {
		when(settingsDao.getSettings()).thenReturn(settingsTable);

		Settings result = settingsRepository.getSettings();

		assertNotNull(result);
		assertEquals(GEMINI_API_KEY, result.getGeminiApiKey());
		verify(settingsDao).getSettings();
	}

	@Test
	void testGetSettings_Global_Null() {
		when(settingsDao.getSettings()).thenReturn(null);

		Settings result = settingsRepository.getSettings();

		assertNull(result);
		verify(settingsDao).getSettings();
	}

	@Test
	void testGetSettings_ByUserId() {
		when(userDao.findById(USER_ID)).thenReturn(userTable);
		when(settingsDao.getSettingsForUser(userTable)).thenReturn(settingsTable);

		Settings result = settingsRepository.getSettings(USER_ID);

		assertNotNull(result);
		assertEquals(GEMINI_API_KEY, result.getGeminiApiKey());
		verify(userDao).findById(USER_ID);
		verify(settingsDao).getSettingsForUser(userTable);
	}

	@Test
	void testGetSettings_ByUserId_Null() {
		when(userDao.findById(USER_ID)).thenReturn(userTable);
		when(settingsDao.getSettingsForUser(userTable)).thenReturn(null);

		Settings result = settingsRepository.getSettings(USER_ID);

		assertNull(result);
		verify(userDao).findById(USER_ID);
		verify(settingsDao).getSettingsForUser(userTable);
	}

	@Test
	void testCreateSettings_Global() {
		when(settingsDao.persist(any(SettingsTable.class))).thenReturn(settingsTable);

		Settings result = settingsRepository.createSettings(settings);

		assertNotNull(result);
		assertEquals(GEMINI_API_KEY, result.getGeminiApiKey());
		verify(settingsDao).persist(any(SettingsTable.class));
	}

	@Test
	void testCreateSettings_ForUser() {
		when(userDao.findById(USER_ID)).thenReturn(userTable);
		when(settingsDao.persist(any(SettingsTable.class))).thenReturn(settingsTable);

		Settings result = settingsRepository.createSettings(USER_ID, settings);

		assertNotNull(result);
		assertEquals(GEMINI_API_KEY, result.getGeminiApiKey());
		verify(userDao).findById(USER_ID);
		verify(settingsDao).persist(any(SettingsTable.class));
	}

	@Test
	void testUpdateSettings_Global() {
		when(settingsDao.getSettings()).thenReturn(settingsTable);
		when(settingsDao.merge(any(SettingsTable.class))).thenReturn(settingsTable);

		Settings result = settingsRepository.updateSettings(settings);

		assertNotNull(result);
		assertEquals(GEMINI_API_KEY, result.getGeminiApiKey());
		verify(settingsDao).getSettings();
		verify(settingsDao).merge(any(SettingsTable.class));
	}

	@Test
	void testUpdateSettings_ForUser() {
		when(userDao.findById(USER_ID)).thenReturn(userTable);
		when(settingsDao.getSettingsForUser(userTable)).thenReturn(settingsTable);
		when(settingsDao.merge(any(SettingsTable.class))).thenReturn(settingsTable);

		Settings result = settingsRepository.updateSettings(USER_ID, settings);

		assertNotNull(result);
		assertEquals(GEMINI_API_KEY, result.getGeminiApiKey());
		verify(userDao).findById(USER_ID);
		verify(settingsDao).getSettingsForUser(userTable);
		verify(settingsDao).merge(any(SettingsTable.class));
	}

	@Test
	void testUpdateSettings_Global_AllButGeminiApiKey() {
		Settings filledSettings = new Settings();
		filledSettings.setModelerClientId(MODELER_CLIENT_ID);
		filledSettings.setModelerClientSecret(MODELER_CLIENT_SECRET);
		filledSettings.setOperateClientId(OPERATE_CLIENT_ID);
		filledSettings.setOperateClientSecret(OPERATE_CLIENT_SECRET);
		filledSettings.setOperateRegionId(OPERATE_REGION_ID);
		filledSettings.setOperateClusterId(OPERATE_CLUSTER_ID);

		SettingsTable filledSettingsTable = getFilledSettingsTable();

		when(settingsDao.getSettings()).thenReturn(settingsTable);
		when(settingsDao.merge(any(SettingsTable.class))).thenReturn(filledSettingsTable);

		Settings result = settingsRepository.updateSettings(filledSettings);

		assertNotNull(result);
		assertEquals(GEMINI_API_KEY, result.getGeminiApiKey());
		assertEquals(MODELER_CLIENT_ID, result.getModelerClientId());
		assertEquals(MODELER_CLIENT_SECRET, result.getModelerClientSecret());
		assertEquals(OPERATE_CLIENT_ID, result.getOperateClientId());
		assertEquals(OPERATE_CLIENT_SECRET, result.getOperateClientSecret());
		assertEquals(OPERATE_REGION_ID, result.getOperateRegionId());
		assertEquals(OPERATE_CLUSTER_ID, result.getOperateClusterId());
		verify(settingsDao).getSettings();
		verify(settingsDao).merge(any(SettingsTable.class));
	}

	private static SettingsTable getFilledSettingsTable() {
		SettingsTable filledSettingsTable = new SettingsTable();
		filledSettingsTable.setGeminiApiKey(GEMINI_API_KEY);
		filledSettingsTable.setModelerClientId(MODELER_CLIENT_ID);
		filledSettingsTable.setModelerClientSecret(MODELER_CLIENT_SECRET);
		filledSettingsTable.setOperateClientId(OPERATE_CLIENT_ID);
		filledSettingsTable.setOperateClientSecret(OPERATE_CLIENT_SECRET);
		filledSettingsTable.setOperateRegionId(OPERATE_REGION_ID);
		filledSettingsTable.setOperateClusterId(OPERATE_CLUSTER_ID);
		return filledSettingsTable;
	}
}
