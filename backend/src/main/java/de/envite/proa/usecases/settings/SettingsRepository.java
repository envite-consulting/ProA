package de.envite.proa.usecases.settings;

import de.envite.proa.entities.Settings;

public interface SettingsRepository {

    Settings getSettings();

    Settings getSettings(Long userId);

    Settings createSettings(Settings settings);

    Settings createSettings(Long userId, Settings settings);

    Settings updateSettings(Settings settings);

    Settings updateSettings(Long userId, Settings settings);
}
