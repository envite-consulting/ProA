package de.envite.proa.bpmn.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.envite.proa.entities.EventType;
import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessEvent;

public class BpmnOperationsTest {

	private BpmnOperations bpmnOperations = new BpmnOperations();

	@Test
	public void testStartEvents() throws IOException {
		// Arrange
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());

		// Act
		List<ProcessEvent> startEvents = bpmnOperations.getStartEvents(bpmnXml);

		// Assert
		assertThat(startEvents)//
				.hasSize(2)//
				.extracting("elementId", "label", "eventType")//
				.contains(//
						tuple("StartEvent_1", "start 1", EventType.START),
						tuple("StartEvent_2", "start 2", EventType.START));
	}

	@Test
	public void testIntermediateCatchEvents() throws IOException {
		// Arrange
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());

		// Act
		List<ProcessEvent> intermediateCatchEvents = bpmnOperations.getIntermediateCatchEvents(bpmnXml);

		// Assert
		assertThat(intermediateCatchEvents)//
				.hasSize(1)//
				.extracting("elementId", "label", "eventType")//
				.contains(tuple("Event_Catch", "catch", EventType.INTERMEDIATE_CATCH));
	}
	
	@Test
	public void testIntermediateThrowEvents() throws IOException {
		// Arrange
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());

		// Act
		List<ProcessEvent> intermediateThrowEvents = bpmnOperations.getIntermediateThrowEvents(bpmnXml);

		// Assert
		assertThat(intermediateThrowEvents)//
				.hasSize(1)//
				.extracting("elementId", "label", "eventType")//
				.contains(tuple("Event_Throw", "throw", EventType.INTERMEDIATE_THROW));
	}
	
	@Test
	public void testEndEvents() throws IOException {
		// Arrange
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());

		// Act
		List<ProcessEvent> endEvents = bpmnOperations.getEndEvents(bpmnXml);

		// Assert
		assertThat(endEvents)//
				.hasSize(2)//
				.extracting("elementId", "label", "eventType")//
				.contains(//
						tuple("EndEvent_1", "end 1", EventType.END),
						tuple("EndEvent_2", "end 2", EventType.END));
	}
	
	@Test
	public void testCallActivites() throws IOException {
		// Arrange
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());

		// Act
		List<ProcessActivity> intermediateThrowEvents = bpmnOperations.getCallActivities(bpmnXml);

		// Assert
		assertThat(intermediateThrowEvents)//
				.hasSize(1)//
				.extracting("elementId", "label")//
				.contains(tuple("Activity_Call", "Call Activity"));
	}
}
