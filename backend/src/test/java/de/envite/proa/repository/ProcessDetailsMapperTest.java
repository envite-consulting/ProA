package de.envite.proa.repository;

import de.envite.proa.entities.process.EventType;
import de.envite.proa.entities.process.ProcessDetails;
import de.envite.proa.repository.processmodel.ProcessDetailsMapper;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProcessDetailsMapperTest {

	private static final long PROCESS_ID = 1L;
	private static final String PROCESS_NAME = "processName";
	private static final String PROCESS_DESCRIPTION = "processDescription";

	private static final String START_EVENT_LABEL = "Start Event Label";
	private static final String START_EVENT_ID = "start-event-id";

	private static final String INTERMEDIATE_THROW_LABEL = "Intermediate Throw Label";
	private static final String INTEREMDIATE_THROW_EVENT_ID = "interemdiate-throw-event-id";

	private static final String INTERMEDIATE_CATCH_EVENT_LABEL = "Intermediate Catch Event Label";
	private static final String INTERMEDIATE_CATCH_EVENT_ID = "intermediate-catch-event-id";

	private static final String END_EVENT_LABEL = "End Event Label";
	private static final String END_EVENT_ID = "end-event-id";

	@Test
	public void testClassInitialization() {
		ProcessDetailsMapper mapper = new ProcessDetailsMapper();
		assertNotNull(mapper);
	}

	@Test
	public void testMap() {

		// Arrange

		ProcessModelTable table = new ProcessModelTable();
		table.setId(PROCESS_ID);
		table.setName(PROCESS_NAME);
		table.setDescription(PROCESS_DESCRIPTION);

		ProcessEventTable startEvent = new ProcessEventTable();
		startEvent.setEventType(EventType.START);
		startEvent.setLabel(START_EVENT_LABEL);
		startEvent.setElementId(START_EVENT_ID);

		ProcessEventTable intermediateThrowEvent = new ProcessEventTable();
		intermediateThrowEvent.setEventType(EventType.INTERMEDIATE_THROW);
		intermediateThrowEvent.setLabel(INTERMEDIATE_THROW_LABEL);
		intermediateThrowEvent.setElementId(INTEREMDIATE_THROW_EVENT_ID);

		ProcessEventTable intermediateCatchEvent = new ProcessEventTable();
		intermediateCatchEvent.setEventType(EventType.INTERMEDIATE_CATCH);
		intermediateCatchEvent.setLabel(INTERMEDIATE_CATCH_EVENT_LABEL);
		intermediateCatchEvent.setElementId(INTERMEDIATE_CATCH_EVENT_ID);

		ProcessEventTable endEvent = new ProcessEventTable();
		endEvent.setEventType(EventType.END);
		endEvent.setLabel(END_EVENT_LABEL);
		endEvent.setElementId(END_EVENT_ID);

		table.setEvents(Arrays.asList(startEvent, intermediateThrowEvent, intermediateCatchEvent, endEvent));

		// Act
		ProcessDetails processDetails = ProcessDetailsMapper.map(table);

		// Assert
		assertThat(processDetails.getId()).isEqualTo(PROCESS_ID);
		assertThat(processDetails.getName()).isEqualTo(PROCESS_NAME);
		assertThat(processDetails.getDescription()).isEqualTo(PROCESS_DESCRIPTION);
		assertThat(processDetails.getStartEvents())//
				.hasSize(1)//
				.extracting("elementId", "label", "eventType")//
				.contains(tuple(START_EVENT_ID, START_EVENT_LABEL, EventType.START));
		assertThat(processDetails.getIntermediateThrowEvents())//
				.hasSize(1)//
				.extracting("elementId", "label", "eventType")//
				.contains(tuple(INTEREMDIATE_THROW_EVENT_ID, INTERMEDIATE_THROW_LABEL, EventType.INTERMEDIATE_THROW));
		assertThat(processDetails.getIntermediateCatchEvents())//
				.hasSize(1)//
				.extracting("elementId", "label", "eventType")//
				.contains(tuple(INTERMEDIATE_CATCH_EVENT_ID, INTERMEDIATE_CATCH_EVENT_LABEL,
						EventType.INTERMEDIATE_CATCH));
		assertThat(processDetails.getEndEvents())//
				.hasSize(1)//
				.extracting("elementId", "label", "eventType")//
				.contains(tuple(END_EVENT_ID, END_EVENT_LABEL, EventType.END));

	}
}
