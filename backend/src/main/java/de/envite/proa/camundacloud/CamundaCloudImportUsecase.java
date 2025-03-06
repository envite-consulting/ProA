package de.envite.proa.camundacloud;

import de.envite.proa.usecases.ProcessOperations;
import de.envite.proa.usecases.processmodel.ProcessModelUsecase;
import de.envite.proa.usecases.processmodel.exceptions.CantReplaceWithCollaborationException;
import de.envite.proa.usecases.processmodel.exceptions.CollaborationAlreadyExistsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.net.URI;

@ApplicationScoped
public class CamundaCloudImportUsecase {

	@RestClient
	private CamundaCloudService camundaCloudService;

	@RestClient
	private CamundaModelerService camundaModelerService;

	@Inject
	private ProcessModelUsecase usecase;

	@Inject
	private ProcessOperations processOperations;

	public Object getProcessModels(CamundaCloudFetchConfiguration configuration) {

		ProcessSearchObject search = new ProcessSearchObject();
		search.getFilter().getUpdatedBy().setEmail(configuration.getEmail());
		return camundaModelerService.getProcessModels("Bearer " + configuration.getToken(), search);
	}

	protected CamundaOperateService createOperateService(String baseUri) {
		return RestClientBuilder
				.newBuilder()
				.baseUri(URI.create(baseUri))
				.build(CamundaOperateService.class);
	}

	public Object getProcessInstances(CamundaCloudFetchConfiguration configuration) {
		String operateUri = "https://" + //
				configuration.getRegionId() + //
				".operate.camunda.io/" + //
				configuration.getClusterId();

		CamundaOperateService camundaOperateService = createOperateService(operateUri);

		String bpmnProcessId = configuration.getBpmnProcessId();

		ProcessInstancesFilter filter = bpmnProcessId != null
				? new ProcessInstancesFilter(bpmnProcessId)
				: new ProcessInstancesFilter();

		return camundaOperateService.getProcessInstances("Bearer " + configuration.getToken(), filter);
	}

	public void importProcessModels(Long projectId, CamundaCloudImportConfiguration config)
			throws CantReplaceWithCollaborationException, CollaborationAlreadyExistsException {
		for (String id : config.getSelectedProcessModelIds()) {
			CamundaProcessModelResponse processModel = camundaModelerService
					.getProcessModel("Bearer " + config.getToken(), id);
			String xml = getProcessXml(processModel);
			String description = processOperations.getDescription(xml);
			boolean isCollaboration = processOperations.getIsCollaboration(xml);

			usecase.saveProcessModel(projectId, processModel.getMetadata().getName(), xml, description,
					isCollaboration);
		}
	}

	private String getProcessXml(CamundaProcessModelResponse processModel) {
		return processModel.getContent();
	}

	public String getToken(CloudCredentials credentials) {
		return camundaCloudService.getToken(credentials).getToken();
	}
}