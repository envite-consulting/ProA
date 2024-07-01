package de.envite.proa.repository;
import de.envite.proa.entities.Settings;
import de.envite.proa.repository.tables.SettingsTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import de.envite.proa.usecases.settings.SettingsRepository;

@ApplicationScoped
public class SettingsRepositoryImpl implements SettingsRepository {

	private final SettingsDao dao;

	@Inject
	public SettingsRepositoryImpl(SettingsDao dao) {
		this.dao = dao;
	}

	@Override
	public Settings getSettings() {
		return map(dao.getSettings());
	}

	@Override
	public Settings createSettings(Settings settings) {
		return map(dao.persist(map(settings)));
	}

	@Override
	public Settings updateSettings(Settings settings) {
		return map(dao.merge(merge(settings)));
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
		SettingsTable table = dao.getSettings();

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
