package de.envite.proa.entities.project;

public class NoResultException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	public NoResultException() {
		super();
	}
	
	public NoResultException(String message) {
		super(message);
	}
}