package de.envite.proa.camundacloud;

import lombok.Data;

@Data
public class ProcessSearchObject {

	private Filter filter = new Filter();
	private int size = 50;
	
	@Data
	static class Filter {

		private UpdatedBy updatedBy = new UpdatedBy();
		private String type = "BPMN";
		
		@Data
		static class UpdatedBy{
			private String email;
		}
	}
}