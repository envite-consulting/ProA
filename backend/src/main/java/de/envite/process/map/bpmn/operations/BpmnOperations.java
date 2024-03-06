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
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Lane;
import org.camunda.bpm.model.bpmn.instance.LaneSet;
import org.camunda.bpm.model.bpmn.instance.Participant;
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

	@Override
	public List<String> getLanes(String processModel) {

		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		System.out.println("laneset1");
		Collection<LaneSet> laneSets = processModelInstance.getModelElementsByType(LaneSet.class);
		printlaneset(laneSets);
		
		Collection<Participant> participants = processModelInstance.getModelElementsByType(Participant.class);//
		
		System.out.println("laneset2");
		for (Participant participant2 : participants) {
			Collection<LaneSet> lanesets2 = participant2.getChildElementsByType(LaneSet.class);
			printlaneset(lanesets2);
		}

		return processModelInstance.getModelElementsByType(Participant.class)//
				.stream()//
				.map(participant -> participant.getName())//
				.collect(Collectors.toList());
	}

	private void printlaneset(Collection<LaneSet> laneSets) {
		for (LaneSet laneSet : laneSets) {
			
			System.out.println(">"+laneSet.getName());

			Collection<Lane> lanes = laneSet.getLanes();
			for (Lane lane : lanes) {

				System.out.println(">"+lane.getName());

			}
		}
	}

	private BpmnModelInstance getProcessModelInstance(String processModel) {
		InputStream processModelStream = new ByteArrayInputStream(processModel.getBytes(StandardCharsets.UTF_8));
		return Bpmn.readModelFromStream(processModelStream);
	}
}
