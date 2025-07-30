package de.envite.proa.rest;

import de.envite.proa.util.ResourceLoader;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;

@Path("")
public class RedirectionResource {

	@Inject
	ResourceLoader resourceLoader;

	/**
	 * Serve index.html for evey call that is not an api call
	 */
	@GET
	@Path("/{path: (?!.*api).+}")
	@Produces("text/html")
	public Response serveVueApp() {
		InputStream indexHtmlStream = resourceLoader.loadResource("META-INF/resources/index.html");
		if (indexHtmlStream == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok(indexHtmlStream).build();
	}
}
