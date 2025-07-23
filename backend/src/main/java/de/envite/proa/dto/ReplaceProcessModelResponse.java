package de.envite.proa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplaceProcessModelResponse {
	private Long replacedProcessModelId;
	private List<ProcessModelChangeResponse> processModelChangeResults;
}