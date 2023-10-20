package de.envite.process.map.bpmn.operations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import de.envite.process.map.entities.ProcessEvent;
import de.envite.process.map.usecases.ProcessOperations;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BpmnOperations implements ProcessOperations {

	@Override
	public List<ProcessEvent> getStartEvents(String processModel) {

		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		Collection<StartEvent> startEvents = processModelInstance.getModelElementsByType(StartEvent.class);
		return startEvents//
				.stream()//
				.map(event -> new ProcessEvent(event.getId(), event.getName()))//
				.collect(Collectors.toList());
	}

	@Override
	public List<ProcessEvent> getEndEvents(String processModel) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		Collection<EndEvent> startEvents = processModelInstance.getModelElementsByType(EndEvent.class);
		return startEvents//
				.stream()//
				.map(event -> new ProcessEvent(event.getId(), event.getName()))//
				.collect(Collectors.toList());
	}

	private BpmnModelInstance getProcessModelInstance(String processModel) {
		InputStream processModelStream = new ByteArrayInputStream(processModel.getBytes(StandardCharsets.UTF_8));
		return Bpmn.readModelFromStream(processModelStream);
	}
}
