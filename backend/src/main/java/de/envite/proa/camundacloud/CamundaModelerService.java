package de.envite.proa.camundacloud;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/api/v1/files/search")
@RegisterRestClient(configKey = "camunda-modeler-service")
public interface CamundaModelerService {

	@POST
	Object getProcessModels(@HeaderParam("Authorization") String authorization, ProcessSearchObject search);
}