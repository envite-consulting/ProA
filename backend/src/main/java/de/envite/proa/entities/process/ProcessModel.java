package de.envite.proa.entities.process;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessModel {

	private String name;

	private String bpmnXml;

	private Set<ProcessEvent> events = new HashSet<>();

	private Set<ProcessActivity> callActivities = new HashSet<>();

	private Set<ProcessDataStore> dataStores = new HashSet<>();

	private String description;

	private String bpmnProcessId;

	private String parentBpmnProcessId;

	private ProcessType processType;
}