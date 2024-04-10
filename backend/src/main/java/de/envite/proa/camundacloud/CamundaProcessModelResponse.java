package de.envite.proa.camundacloud;

import lombok.Data;

@Data
public class CamundaProcessModelResponse {

	private MetaData metadata;
	private String content;
	
	@Data
	static class MetaData{
		private String name;
	}
}
