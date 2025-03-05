package de.envite.proa.entities.process;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessEvent {

	private String elementId;
	private String label;
	private EventType eventType;
}