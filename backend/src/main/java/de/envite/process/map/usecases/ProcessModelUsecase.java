package de.envite.process.map.usecases;

import java.util.ArrayList;
import java.util.List;

import de.envite.process.map.entities.ProcessDetails;
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

	public Long saveProcessModel(String name, String xml, String description) {

		List<ProcessEvent> startEvents = processOperations.getStartEvents(xml);
		List<ProcessEvent> endEvents = processOperations.getEndEvents(xml);

		ProcessModel processModel = new ProcessModel(name, //
				xml, //
				startEvents, //
				new ArrayList<>(), //
				endEvents, //
				description//
		);

		return repository.saveProcessModel(processModel);
	}

	public String getProcessModel(Long id) {
		return repository.getProcessModel(id);
	}

	public List<ProcessInformation> getProcessInformation() {
		return repository.getProcessInformation();
	}

	public ProcessDetails getProcessDetails(Long id) {
		return repository.getProcessDetails(id);
	}
}