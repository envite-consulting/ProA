package de.envite.proa.entities.process;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDetails {

	private Long id;

	private String name;

	private String bpmnProcessId;

	private Set<ProcessEvent> startEvents = new HashSet<>();

	private Set<ProcessEvent> intermediateCatchEvents = new HashSet<>();

	private Set<ProcessEvent> intermediateThrowEvents = new HashSet<>();

	private Set<ProcessEvent> endEvents = new HashSet<>();

	private Set<ProcessActivity> activities = new HashSet<>();

	private String description;
}