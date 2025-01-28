
package de.envite.proa.rest;

import de.envite.proa.entities.Settings;
import de.envite.proa.security.RolesAllowedIfWebVersion;
import de.envite.proa.usecases.settings.SettingsUsecase;
import jakarta.ws.rs.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/settings")
public class SettingsResource {

	@Inject
	SettingsUsecase usecase;

	@Inject
	JsonWebToken jwt;

	@Inject
	@ConfigProperty(name = "app.mode", defaultValue = "desktop")
	String appMode;

	@GET
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public Settings getSettings() {
		if (appMode.equals("web")) {
			return usecase.getSettings(Long.parseLong(jwt.getClaim("userId").toString()));
		}
		return usecase.getSettings();
	}

	@POST
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public Settings createSettings(Settings settings) {
		if (appMode.equals("web")) {
			return usecase.createSettings(Long.parseLong(jwt.getClaim("userId").toString()), settings);
		}
		return usecase.createSettings(settings);
	}

	@PATCH
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public Settings updateSettings(Settings settings) {
		if (appMode.equals("web")) {
			return usecase.updateSettings(Long.parseLong(jwt.getClaim("userId").toString()), settings);
		}
		return usecase.updateSettings(settings);
	}
}
