package de.envite.proa.camundacloud;

import lombok.Data;

@Data
public class CamundaCloudFetchConfiguration {

	private String token;
	private String email;
	private String regionId;
	private String clusterId;
	private String bpmnProcessId;
}