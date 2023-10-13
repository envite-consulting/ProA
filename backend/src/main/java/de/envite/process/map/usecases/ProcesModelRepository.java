package de.envite.process.map.usecases;

import java.util.List;

import de.envite.process.map.entities.ProcessInformation;

public interface ProcesModelRepository {

	Long saveProcessModel(String name, String xml);

	String getProcessModel(Long id);

	List<ProcessInformation> getProcessModels();
}