package de.envite.proa.usecases.processmap;

import de.envite.proa.entities.ProcessMap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProcessMapUsecase {
	
	@Inject
	private ProcessMapRespository repository;
	
	public ProcessMap getProcessMap() {
		return repository.getProcessMap();
	}
}
