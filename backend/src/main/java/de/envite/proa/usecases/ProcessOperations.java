package de.envite.proa.usecases;

import java.util.List;

import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessDataStore;
import de.envite.proa.entities.ProcessEvent;

public interface ProcessOperations {

	public List<ProcessEvent> getStartEvents(String processModel);
	
	public List<ProcessEvent> getEndEvents(String processModel);

	public List<ProcessActivity> getCallActivities(String xml);

	public List<ProcessEvent> getIntermediateThrowEvents(String xml);

	public List<ProcessEvent> getIntermediateCatchEvents(String xml);

	public List<ProcessDataStore> getDataStores(String xml);

	public String getDescription(String xml);

	public String getBpmnProcessId(String xml);
}