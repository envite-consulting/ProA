package de.envite.proa.repository.settings;
import de.envite.proa.entities.settings.Settings;
import de.envite.proa.repository.user.UserDao;
import de.envite.proa.repository.tables.SettingsTable;
import de.envite.proa.repository.tables.UserTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import de.envite.proa.usecases.settings.SettingsRepository;

@ApplicationScoped
public class SettingsRepositoryImpl implements SettingsRepository {

	private final SettingsDao settingsDao;
    private final UserDao userDao;

	@Inject
	public SettingsRepositoryImpl(SettingsDao settingsDao, UserDao userDao) {
		this.settingsDao = settingsDao;
        this.userDao = userDao;
	}

	@Override
	public Settings getSettings() {
		SettingsTable settings = settingsDao.getSettings();
		if (settings == null) {
			return null;
		}
		return map(settings);
	}

	@Override
	public Settings getSettings(Long userId) {
		UserTable userTable = userDao.findById(userId);
		SettingsTable settings = settingsDao.getSettingsForUser(userTable);
		if (settings == null) {
			return null;
		}
		return map(settings);
	}

	@Override
	public Settings createSettings(Settings settings) {
		return map(settingsDao.persist(map(settings)));
	}

	@Override
	public Settings createSettings(Long userId, Settings settings) {
		SettingsTable table = map(settings);
		table.setUser(userDao.findById(userId));
		return map(settingsDao.persist(table));
	}

	@Override
	public Settings updateSettings(Settings settings) {
		return map(settingsDao.merge(merge(settings)));
	}

	@Override
	public Settings updateSettings(Long userId, Settings settings) {
		SettingsTable table = merge(userId, settings);
		return map(settingsDao.merge(table));
	}

	private Settings map(SettingsTable table) {
		Settings settings = new Settings();
		settings.setGeminiApiKey(table.getGeminiApiKey());
		settings.setModelerClientId(table.getModelerClientId());
		settings.setModelerClientSecret(table.getModelerClientSecret());
		settings.setOperateClientId(table.getOperateClientId());
		settings.setOperateClientSecret(table.getOperateClientSecret());
		settings.setOperateRegionId(table.getOperateRegionId());
		settings.setOperateClusterId(table.getOperateClusterId());
		return settings;
	}

	private SettingsTable map(Settings settings) {
		SettingsTable table = new SettingsTable();
		table.setGeminiApiKey(settings.getGeminiApiKey());
		table.setModelerClientId(settings.getModelerClientId());
		table.setModelerClientSecret(settings.getModelerClientSecret());
		table.setOperateClientId(settings.getOperateClientId());
		table.setOperateClientSecret(settings.getOperateClientSecret());
		table.setOperateRegionId(settings.getOperateRegionId());
		table.setOperateClusterId(settings.getOperateClusterId());
		return table;
	}

	private SettingsTable merge(Settings settings) {
		SettingsTable table = settingsDao.getSettings();

		return merge(table, settings);
	}

	private SettingsTable merge(Long userId, Settings settings) {
		SettingsTable table = settingsDao.getSettingsForUser(userDao.findById(userId));
		return merge(table, settings);
	}

	private SettingsTable merge(SettingsTable table, Settings settings) {
		if (settings.getGeminiApiKey() != null) {
			table.setGeminiApiKey(settings.getGeminiApiKey());
		}
		if (settings.getModelerClientId() != null) {
			table.setModelerClientId(settings.getModelerClientId());
		}
		if (settings.getModelerClientSecret() != null) {
			table.setModelerClientSecret(settings.getModelerClientSecret());
		}
		if (settings.getOperateClientId() != null) {
			table.setOperateClientId(settings.getOperateClientId());
		}
		if (settings.getOperateClientSecret() != null) {
			table.setOperateClientSecret(settings.getOperateClientSecret());
		}
		if (settings.getOperateRegionId() != null) {
			table.setOperateRegionId(settings.getOperateRegionId());
		}
		if (settings.getOperateClusterId() != null) {
			table.setOperateClusterId(settings.getOperateClusterId());
		}

		return table;
	}
}
