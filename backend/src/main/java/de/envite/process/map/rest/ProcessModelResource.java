package de.envite.process.map.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import de.envite.process.map.entities.ProcessInformation;
import de.envite.process.map.entities.ProcessMap;
import de.envite.process.map.usecases.ProcessModelUsecase;
import de.envite.process.map.usecases.processmap.ProcessMapUsecase;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/process-model")
public class ProcessModelResource {

	@Inject
	private ProcessModelUsecase usecase;

	/**
	 * 
	 * Creates a new process model 
	 * 
	 * @param processModel the bpmn file
	 * @param fileName the file name of the bpmn file
	 * @param description 
	 * @return
	 */
	@POST
	public Long uploadProcessModel(@RestForm File processModel, @RestForm String fileName,
			@RestForm String description) {

		String content = readFileToString(processModel);

		return usecase.saveProcessModel(fileName, content);
	}

	/**
	 * Gets the xml representations of the bpmn file the id corresponds to.
	 * This method is called from the process model view that shows the bpmn via bpmn.io
	 * 
	 * @param id of the bpmn file
	 * @return bpmn file as string
	 */
	@Path("/{id}")
	@GET
	public String getProcessModel(@RestPath Long id) {
		return usecase.getProcessModel(id);
	}

	/**
	 * This methods gets the names and the corresponding ids of all process models
	 * in order to show them as a list in the process list in the frontend
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ProcessInformation> getProcessModels() {
		return usecase.getProcessModels();
	}

	
	private String readFileToString(File file) {
		StringBuilder contentBuilder = new StringBuilder();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {

			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				contentBuilder.append(sCurrentLine).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}
}