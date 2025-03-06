package de.envite.proa.rest;

import de.envite.proa.camundacloud.CamundaCloudFetchConfiguration;
import de.envite.proa.camundacloud.CamundaCloudImportConfiguration;
import de.envite.proa.camundacloud.CamundaCloudImportUsecase;
import de.envite.proa.camundacloud.CloudCredentials;
import de.envite.proa.security.RolesAllowedIfWebVersion;
import de.envite.proa.usecases.processmodel.exceptions.CantReplaceWithCollaborationException;
import de.envite.proa.usecases.processmodel.exceptions.CollaborationAlreadyExistsException;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.jboss.resteasy.reactive.RestPath;

@Path("/api/camunda-cloud")
public class CamundaCloudImportResource {

	@Inject
	private CamundaCloudImportUsecase usecase;

	@POST
	@Path("/token")
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public Object getToken(CloudCredentials credentials) {
		return usecase.getToken(credentials);
	}

	@POST
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public Object uploadProcessModel(CamundaCloudFetchConfiguration configuration) {
		return usecase.getProcessModels(configuration);
	}

	@POST
	@Path("/process-instances")
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public Object getProcessInstances(CamundaCloudFetchConfiguration configuration) {
		return usecase.getProcessInstances(configuration);
	}

	@POST
	@Path("/project/{projectId}/import")
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public void importProcessModels(@RestPath Long projectId, CamundaCloudImportConfiguration config)
			throws CantReplaceWithCollaborationException, CollaborationAlreadyExistsException {
		usecase.importProcessModels(projectId, config);
	}
}