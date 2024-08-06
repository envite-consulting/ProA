package de.envite.proa.camundacloud;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/v1/process-instances")
@RegisterRestClient
public interface CamundaOperateService {

	@POST
	@Path("/search")
	Object getProcessInstances(@HeaderParam("Authorization") String authorization, ProcessInstancesFilter filter);
}