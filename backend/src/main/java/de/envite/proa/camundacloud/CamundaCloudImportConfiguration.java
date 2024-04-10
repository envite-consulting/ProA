package de.envite.proa.camundacloud;

import java.util.List;

import lombok.Data;

@Data
public class CamundaCloudImportConfiguration {
	
	private String token;
	private List<String> selectedProcessModelIds;
}
