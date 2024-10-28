
package de.envite.proa.rest;

import de.envite.proa.entities.Settings;
import de.envite.proa.usecases.settings.SettingsUsecase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestPath;

@Path("/api/settings")
public class SettingsResource {

	@Inject
	SettingsUsecase usecase;

	@Inject
	@ConfigProperty(name = "app.mode", defaultValue = "desktop")
	String appMode;

	@GET
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	public Settings getSettings() {
		if (appMode.equals("web")) {
			throw new ForbiddenException("Not allowed in web mode");
		}
		return usecase.getSettings();
	}

	@GET
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({"User", "Admin"})
	public Settings getSettingsForUser(@RestPath Long userId) {
		return usecase.getSettings(userId);
	}

	@POST
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Settings createSettings(Settings settings) {
		if (appMode.equals("web")) {
			throw new ForbiddenException("Not allowed in web mode");
		}
		return usecase.createSettings(settings);
	}

	@POST
	@Path("/{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({"User", "Admin"})
	public Settings createSettings(@RestPath Long userId, Settings settings) {
		return usecase.createSettings(userId, settings);
	}

	@PATCH
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Settings updateSettings(Settings settings) {
		if (appMode.equals("web")) {
			throw new ForbiddenException("Not allowed in web mode");
		}
		return usecase.updateSettings(settings);
	}

	@PATCH
	@Path("/{userId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({"User", "Admin"})
	public Settings updateSettings(@RestPath Long userId, Settings settings) {
		return usecase.updateSettings(userId, settings);
	}
}
