package de.envite.proa.camundacloud;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessInstancesFilter {

	private Filter filter;
	private Integer size;

	public ProcessInstancesFilter() {
		this.size = 1;
	}

	public ProcessInstancesFilter(String bpmnProcessId) {
		this.filter = new Filter(bpmnProcessId);
		this.size = 1000;
	}

	@Data
	static class Filter {
		private String state = "ACTIVE";
		private String bpmnProcessId;

		public Filter(String bpmnProcessId) {
			this.bpmnProcessId = bpmnProcessId;
		}
	}
}