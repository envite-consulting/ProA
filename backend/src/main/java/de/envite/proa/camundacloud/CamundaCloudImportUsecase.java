package de.envite.proa.camundacloud;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CamundaCloudImportUsecase {

	@RestClient
	private CamundaCloudService camundaCloudService;

	@RestClient
	private CamundaModelerService camundaModelerService;

	public Object getProcessModels(CamundaCloudImportConfiguration configuration) {
		
		CloudCredentials credentials = new CloudCredentials();
		credentials.setClientId(configuration.getClientId());
		credentials.setClientSecret(configuration.getClientSecret());
		
		String token = camundaCloudService.getToken(credentials).getToken();

		ProcessSearchObject search = new ProcessSearchObject();
		search.getFilter().getUpdatedBy().setEmail(configuration.getEmail());
		return camundaModelerService.getProcessModels("Bearer " + token, search);
	}
}