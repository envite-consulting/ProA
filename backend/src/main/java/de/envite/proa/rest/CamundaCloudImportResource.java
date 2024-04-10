package de.envite.proa.rest;

import de.envite.proa.camundacloud.CamundaCloudFetchConfiguration;
import de.envite.proa.camundacloud.CamundaCloudImportConfiguration;
import de.envite.proa.camundacloud.CamundaCloudImportUsecase;
import de.envite.proa.camundacloud.CloudCredentials;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/camunda-cloud")
public class CamundaCloudImportResource {
	
	@Inject
	private CamundaCloudImportUsecase usecase;

	@POST
	@Path("/token")
	public Object getToken(CloudCredentials credentials) {

		return usecase.getToken(credentials);
	}
	
	@POST
	public Object uploadProcessModel(CamundaCloudFetchConfiguration configuration) {

		return usecase.getProcessModels(configuration);
	}
	
	@POST
	@Path("/import")
	public void importProcessModels(CamundaCloudImportConfiguration config) {

		usecase.importProcessModels(config);
	}
}