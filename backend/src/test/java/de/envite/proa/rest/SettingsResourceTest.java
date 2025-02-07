package de.envite.proa.rest;

import de.envite.proa.entities.settings.Settings;
import de.envite.proa.usecases.settings.SettingsUsecase;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SettingsResourceTest {

	@InjectMocks
	private SettingsResource resource;

	@Mock
	private SettingsUsecase usecase;

	@Mock
	private JsonWebToken jwt;

	private static final Long USER_ID = 1L;
	private static final String APP_MODE_WEB = "web";
	private static final String APP_MODE_DESKTOP = "desktop";
	private static final Settings SETTINGS = new Settings();

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetSettingsWebMode() {
		resource.appMode = APP_MODE_WEB;
		when(jwt.getClaim("userId")).thenReturn(USER_ID.toString());
		when(usecase.getSettings(USER_ID)).thenReturn(SETTINGS);

		Settings result = resource.getSettings();

		assertEquals(SETTINGS, result);
		verify(jwt, times(1)).getClaim("userId");
		verify(usecase, times(1)).getSettings(USER_ID);
	}

	@Test
	public void testGetSettingsDesktopMode() {
		resource.appMode = APP_MODE_DESKTOP;
		when(usecase.getSettings()).thenReturn(SETTINGS);

		Settings result = resource.getSettings();

		assertEquals(SETTINGS, result);
		verify(usecase, times(1)).getSettings();
		verify(jwt, never()).getClaim(anyString());
	}

	@Test
	public void testCreateSettingsWebMode() {
		resource.appMode = APP_MODE_WEB;
		when(jwt.getClaim("userId")).thenReturn(USER_ID.toString());
		when(usecase.createSettings(USER_ID, SETTINGS)).thenReturn(SETTINGS);

		Settings result = resource.createSettings(SETTINGS);

		assertEquals(SETTINGS, result);
		verify(jwt, times(1)).getClaim("userId");
		verify(usecase, times(1)).createSettings(USER_ID, SETTINGS);
	}

	@Test
	public void testCreateSettingsDesktopMode() {
		resource.appMode = APP_MODE_DESKTOP;
		when(usecase.createSettings(SETTINGS)).thenReturn(SETTINGS);

		Settings result = resource.createSettings(SETTINGS);

		assertEquals(SETTINGS, result);
		verify(usecase, times(1)).createSettings(SETTINGS);
		verify(jwt, never()).getClaim(anyString());
	}

	@Test
	public void testUpdateSettingsWebMode() {
		resource.appMode = APP_MODE_WEB;
		when(jwt.getClaim("userId")).thenReturn(USER_ID.toString());
		when(usecase.updateSettings(USER_ID, SETTINGS)).thenReturn(SETTINGS);

		Settings result = resource.updateSettings(SETTINGS);

		assertEquals(SETTINGS, result);
		verify(jwt, times(1)).getClaim("userId");
		verify(usecase, times(1)).updateSettings(USER_ID, SETTINGS);
	}

	@Test
	public void testUpdateSettingsDesktopMode() {
		resource.appMode = APP_MODE_DESKTOP;
		when(usecase.updateSettings(SETTINGS)).thenReturn(SETTINGS);

		Settings result = resource.updateSettings(SETTINGS);

		assertEquals(SETTINGS, result);
		verify(usecase, times(1)).updateSettings(SETTINGS);
		verify(jwt, never()).getClaim(anyString());
	}
}
