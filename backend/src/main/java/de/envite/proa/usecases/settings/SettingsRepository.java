package de.envite.proa.usecases.settings;

import de.envite.proa.entities.Settings;

public interface SettingsRepository {

    Settings getSettings();

    Settings createSettings(Settings settings);

    Settings updateSettings(Settings settings);
}
