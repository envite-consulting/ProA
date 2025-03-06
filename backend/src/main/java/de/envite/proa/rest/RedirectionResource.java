package de.envite.proa.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.io.InputStream;

@Path("")
public class RedirectionResource {

	/**
	 * Serve index.html for evey call that is not an api call
	 */
	@GET
	@Path("/{path: (?!.*api).+}")
	@Produces("text/html")
	public Response serveVueApp() {
		InputStream indexHtmlStream = getClass().getClassLoader().getResourceAsStream("META-INF/resources/index.html");
		if (indexHtmlStream == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok(indexHtmlStream).build();
	}
}
