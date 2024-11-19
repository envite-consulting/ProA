package de.envite.proa.rest;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.Response;

@Path("")
public class RedirectionResource {

	/**
	 * Redirect every Call that is not an api call to the vue.js landing page
	 * @return
	 */
	@GET
	@Path("/{path: (?!.*api).+}")
	public Response redirectToLandingPage() {
		return Response//
				.status(Response.Status.SEE_OTHER).header("Location", "/").build();
	}
}
