package de.envite.proa.camundacloud;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/api/v1/files")
@RegisterRestClient(configKey = "camunda-modeler-service")
public interface CamundaModelerService {

	@POST
	@Path("/search")
	Object getProcessModels(@HeaderParam("Authorization") String authorization, ProcessSearchObject search);

	@GET
	@Path("/{id}")
	CamundaProcessModelResponse getProcessModel(@HeaderParam("Authorization") String authorization, @PathParam("id") String id);
	
}