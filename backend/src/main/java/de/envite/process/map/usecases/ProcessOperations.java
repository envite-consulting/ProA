package de.envite.process.map.usecases;

import java.util.List;

import de.envite.process.map.entities.ProcessEvent;

public interface ProcessOperations {

	public List<ProcessEvent> getStartEvents(String processModel);
	
	public List<ProcessEvent> getEndEvents(String processModel);
}