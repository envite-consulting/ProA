package de.envite.proa.entities.project;

public class AccessDeniedException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	public AccessDeniedException() {
		super();
	}

	public AccessDeniedException(String message) {
		super(message);
	}
}