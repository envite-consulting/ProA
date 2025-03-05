package de.envite.proa.usecases.settings;

import de.envite.proa.entities.settings.Settings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SettingsUsecase {

    @Inject
    SettingsRepository repository;

    public Settings getSettings() {
        return repository.getSettings();
    }

    public Settings getSettings(Long userId) {
        return repository.getSettings(userId);
    }

    public Settings createSettings(Settings settings) {
        return repository.createSettings(settings);
    }

    public Settings createSettings(Long userId, Settings settings) {
        return repository.createSettings(userId, settings);
    }

    public Settings updateSettings(Settings settings) {
        return repository.updateSettings(settings);
    }

    public Settings updateSettings(Long userId, Settings settings) {
        return repository.updateSettings(userId, settings);
    }
}