package de.envite.proa.camundacloud;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/oauth/token")
@RegisterRestClient(configKey="camunda-cloud-service")
public interface CamundaCloudService {
	
	@POST
	TokenResponse getToken(CloudCredentials credentials);
}
