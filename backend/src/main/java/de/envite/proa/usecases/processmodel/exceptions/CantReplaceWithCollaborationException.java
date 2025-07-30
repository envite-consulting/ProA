package de.envite.proa.usecases.processmodel.exceptions;

import lombok.Getter;

@Getter
public class CantReplaceWithCollaborationException extends Exception {
	private final String exceptionType = "CantReplaceWithCollaborationException";

	public CantReplaceWithCollaborationException(Long processModelId) {
		super("Can't replace existing process model with ID " + processModelId + " with a collaboration.");
	}

}
