package de.envite.proa.rest;

import de.envite.proa.entities.Settings;
import de.envite.proa.usecases.settings.SettingsUsecase;
import jakarta.ws.rs.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

@Path("/settings")
public class SettingsResource {

	@Inject
	SettingsUsecase usecase;

	@GET
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	public Settings getSettings() {
		return usecase.getSettings();
	}

	@POST
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Settings createSettings(Settings settings) {
		return usecase.createSettings(settings);
	}

	@PATCH
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Settings updateSettings(Settings settings) {
		return usecase.updateSettings(settings);
	}
}
