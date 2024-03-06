package de.envite.proa.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.usecases.ProcessModelUsecase;
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
	 * @return id of saved process model
	 */
	@POST
	public Long uploadProcessModel(@RestForm File processModel, @RestForm String fileName,
			@RestForm String description) {

		String content = readFileToString(processModel);
		fileName = fileName.replace(".bpmn", "");
		return usecase.saveProcessModel(fileName, content, description);
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
	public List<ProcessInformation> getProcessInformation() {
		return usecase.getProcessInformation();
	}
	
	@GET
	@Path("/{id}/details")
	@Produces(MediaType.APPLICATION_JSON)
	public ProcessDetails getProcessDetails(@RestPath Long id) {
		return usecase.getProcessDetails(id);
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