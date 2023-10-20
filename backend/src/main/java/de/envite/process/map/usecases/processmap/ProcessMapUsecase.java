package de.envite.process.map.usecases.processmap;

import de.envite.process.map.entities.ProcessMap;
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
