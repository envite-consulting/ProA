package de.envite.proa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessModelChangeResponse {
	private String processModelName;
	private Integer processModelLevel;
	private String message;
}