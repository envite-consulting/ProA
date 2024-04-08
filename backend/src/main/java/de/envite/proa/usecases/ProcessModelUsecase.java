package de.envite.proa.usecases;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessDataStore;
import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessEvent;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.entities.ProcessModel;
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
		List<ProcessEvent> intermediateThrowEvents = processOperations.getIntermediateThrowEvents(xml);
		List<ProcessEvent> intermediateCatchEvents = processOperations.getIntermediateCatchEvents(xml);
		List<ProcessEvent> endEvents = processOperations.getEndEvents(xml);
		List<ProcessEvent> events = Stream.of(startEvents, //
				intermediateThrowEvents, //
				intermediateCatchEvents, //
				endEvents//
		).flatMap(Collection::stream).collect(Collectors.toList());
		List<ProcessActivity> callActivities = processOperations.getCallActivities(xml);
		List<ProcessDataStore> dataStores = processOperations.getDataStores(xml);

		ProcessModel processModel = new ProcessModel(name, //
				xml, //
				events, //
				callActivities, //
				dataStores, //
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

	public void deleteProcessModel(Long id) {
		repository.deleteProcessModel(id);
	}
}