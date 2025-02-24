package de.envite.proa.usecases.processmodel.exceptions;

import lombok.Getter;

@Getter
public class CollaborationAlreadyExistsException extends Exception {
	private final String exceptionType = "CollaborationAlreadyExistsException";
	private final String name;

	public CollaborationAlreadyExistsException(String bpmnId, String name) {
		super("Collaboration with bpmnId " + bpmnId + " or name " + name + " already exists.");
		this.name = name;
	}

}
