package de.envite;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {
	
	@Inject
	private Persistence persistence;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from RESTEasy Reactive 2";
    }
    
    @GET
    @Path("/storage")
    @Produces(MediaType.TEXT_PLAIN)
    public List<MyEntity> hello2() {
    	
    	return persistence.doSomething();
    }
}