package de.envite.proa.usecases.settings;

import de.envite.proa.entities.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SettingsUsecaseTest {

    private static final long USER_ID = 1L;
    private static final String GEMINI_API_KEY = "geminiApiKey";
    private static final String MODELER_CLIENT_ID = "modelerClientId";
    private static final String MODELER_CLIENT_SECRET = "modelerClientSecret";

    private static Settings settings;

    @Mock
    private SettingsRepository repository;

    @InjectMocks
    private SettingsUsecase usecase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        settings = new Settings();
        settings.setGeminiApiKey(GEMINI_API_KEY);
        settings.setModelerClientId(MODELER_CLIENT_ID);
        settings.setModelerClientSecret(MODELER_CLIENT_SECRET);
    }

    @Test
    void testGetSettings() {
        when(repository.getSettings()).thenReturn(settings);

        Settings result = usecase.getSettings();

        assertNotNull(result);
        assertEquals(settings, result);
        verify(repository, times(1)).getSettings();
    }

    @Test
    void testGetSettingsByUserId() {
        when(repository.getSettings(USER_ID)).thenReturn(settings);

        Settings result = usecase.getSettings(USER_ID);

        assertNotNull(result);
        assertEquals(settings, result);
        verify(repository, times(1)).getSettings(USER_ID);
    }

    @Test
    void testCreateSettings() {
        when(repository.createSettings(settings)).thenReturn(settings);

        Settings result = usecase.createSettings(settings);

        assertNotNull(result);
        assertEquals(settings, result);
        verify(repository, times(1)).createSettings(settings);
    }

    @Test
    void testCreateSettingsWithUserId() {
        when(repository.createSettings(USER_ID, settings)).thenReturn(settings);

        Settings result = usecase.createSettings(USER_ID, settings);

        assertNotNull(result);
        assertEquals(settings, result);
        verify(repository, times(1)).createSettings(USER_ID, settings);
    }

    @Test
    void testUpdateSettings() {
        when(repository.updateSettings(settings)).thenReturn(settings);

        Settings result = usecase.updateSettings(settings);

        assertNotNull(result);
        assertEquals(settings, result);
        verify(repository, times(1)).updateSettings(settings);
    }

    @Test
    void testUpdateSettingsWithUserId() {
        when(repository.updateSettings(USER_ID, settings)).thenReturn(settings);

        Settings result = usecase.updateSettings(USER_ID, settings);

        assertNotNull(result);
        assertEquals(settings, result);
        verify(repository, times(1)).updateSettings(USER_ID, settings);
    }
}
