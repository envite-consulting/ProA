package de.envite.proa.camundacloud;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import de.envite.proa.usecases.ProcessModelUsecase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CamundaCloudImportUsecase {

	@RestClient
	private CamundaCloudService camundaCloudService;

	@RestClient
	private CamundaModelerService camundaModelerService;

	@Inject
	private ProcessModelUsecase usecase;

	public Object getProcessModels(CamundaCloudFetchConfiguration configuration) {

		ProcessSearchObject search = new ProcessSearchObject();
		search.getFilter().getUpdatedBy().setEmail(configuration.getEmail());
		return camundaModelerService.getProcessModels("Bearer " + configuration.getToken(), search);
	}

	public void importProcessModels(Long projectId, CamundaCloudImportConfiguration config) {

		for (String id : config.getSelectedProcessModelIds()) {
			CamundaProcessModelResponse processModel = camundaModelerService
					.getProcessModel("Bearer " + config.getToken(), id);
			usecase.saveProcessModel(projectId, processModel.getMetadata().getName(), getProcessXml(processModel), "");
		}
	}

	private String getProcessXml(CamundaProcessModelResponse processModel) {
		String content = processModel.getContent();
		return content;
	}

	public String getToken(CloudCredentials credentials) {
		return camundaCloudService.getToken(credentials).getToken();
	}
}