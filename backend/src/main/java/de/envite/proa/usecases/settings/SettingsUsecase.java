package de.envite.proa.usecases.settings;

import de.envite.proa.entities.Settings;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SettingsUsecase {

    @Inject
    SettingsRepository repository;

    public Settings getSettings() {
        return repository.getSettings();
    }

    public Settings createSettings(Settings settings) {
        return repository.createSettings(settings);
    }

    public Settings updateSettings(Settings settings) {
        return repository.updateSettings(settings);
    }
}