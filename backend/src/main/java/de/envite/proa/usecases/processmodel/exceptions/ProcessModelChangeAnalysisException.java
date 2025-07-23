package de.envite.proa.usecases.processmodel.exceptions;

import lombok.Getter;

@Getter
public class ProcessModelChangeAnalysisException extends RuntimeException {
	private final String exceptionType = "ProcessModelChangeAnalysisException";

	public ProcessModelChangeAnalysisException(String message) {
		super(message);
	}
}