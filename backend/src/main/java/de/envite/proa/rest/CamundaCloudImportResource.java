package de.envite.proa.rest;

import de.envite.proa.camundacloud.CamundaCloudImportConfiguration;
import de.envite.proa.camundacloud.CamundaCloudImportUsecase;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/camunda-cloud")
public class CamundaCloudImportResource {
	
	@Inject
	private CamundaCloudImportUsecase usecase;

	@POST
	public Object uploadProcessModel(CamundaCloudImportConfiguration configuration) {

		return usecase.getProcessModels(configuration);
	}
}
