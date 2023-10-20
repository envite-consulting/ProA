package de.envite.process.map.usecases;

import java.util.ArrayList;
import java.util.List;

import de.envite.process.map.entities.ProcessEvent;
import de.envite.process.map.entities.ProcessInformation;
import de.envite.process.map.entities.ProcessModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProcessModelUsecase {

	@Inject
	private ProcessModelRepository repository;

	@Inject
	private ProcessOperations processOperations;

	public Long saveProcessModel(String name, String xml) {

		List<ProcessEvent> startEvents = processOperations.getStartEvents(xml);
		List<ProcessEvent> endEvents = processOperations.getEndEvents(xml);

		ProcessModel processModel = new ProcessModel(name, xml, startEvents, new ArrayList<>(), endEvents);

		return repository.saveProcessModel(processModel);
	}

	public String getProcessModel(Long id) {
		return repository.getProcessModel(id);
	}

	public List<ProcessInformation> getProcessModels() {
		return repository.getProcessModels();
	}
}