package de.envite.process.map.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import de.envite.process.map.entities.ProcessInformation;
import de.envite.process.map.usecases.ProcessModelUsecase;
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

	@POST
	public Long uploadProcessModel(@RestForm File processModel, @RestForm String fileName, @RestForm String description) {


		String content = readFileToString(processModel);

		return usecase.saveProcessModel(fileName, content);
	}
	
	@Path("/{id}")
	@GET
	public String getProcessModel(@RestPath Long id) {
		return usecase.getProcessModel(id);
	}
	
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