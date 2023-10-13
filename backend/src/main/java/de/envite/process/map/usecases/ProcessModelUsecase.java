package de.envite.process.map.usecases;

import java.util.List;

import de.envite.process.map.entities.ProcessInformation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProcessModelUsecase {

	@Inject
	private ProcesModelRepository repository;
	
	public Long saveProcessModel(String name, String xml) {
		return repository.saveProcessModel(name, xml);
	}
	
	public String getProcessModel(Long id) {
		return repository.getProcessModel(id);
	}

	public List<ProcessInformation> getProcessModels() {
		return repository.getProcessModels();
	}
}
