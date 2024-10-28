package de.envite.proa.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.envite.proa.security.RolesAllowedIfWebVersion;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.usecases.ProcessModelUsecase;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api")
public class ProcessModelResource {

	@Inject
	private ProcessModelUsecase usecase;

	/**
	 * 
	 * Creates a new process model
	 * 
	 * @param projectId
	 *            the id of the project the process model belongs to
	 * @param processModel
	 *            the bpmn file
	 * @param fileName
	 *            the file name of the bpmn file
	 * @param description
	 *            the description of the process
	 * @return id of saved process model
	 */
	@POST
	@Path("/project/{projectId}/process-model")
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public Response uploadProcessModel(@RestPath Long projectId, @RestForm File processModel, @RestForm String fileName,
			@RestForm String description, @RestForm String isCollaboration) {
		try {
			String content = readFileToString(processModel);
			fileName = fileName.replace(".bpmn", "");
			return Response //
					.ok(usecase.saveProcessModel( //
							projectId, //
							fileName, //
							content, //
							description, //
							isCollaboration, //
							null //
					)) //
					.build();
		} catch (IllegalArgumentException e) {
			Map<String, String> errorResponse = new HashMap<>();
			String errorMessage = e.getMessage();
			String[] splitMessage = errorMessage.split(" ");
			errorResponse.put("error", errorMessage);
			errorResponse.put("data", splitMessage[splitMessage.length - 1]);
			return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
		}
	}

	@Path("project/{projectId}/process-model/{oldProcessId}")
	@POST
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public Long replaceProcessModel(@RestPath Long projectId, @RestPath Long oldProcessId, @RestForm File processModel,
			@RestForm String fileName, @RestForm String description) {
		String content = readFileToString(processModel);
		fileName = fileName.replace(".bpmn", "");
		return usecase.replaceProcessModel(projectId, oldProcessId, fileName, content, description, null);
	}

	/**
	 * Gets the xml representations of the bpmn file the id corresponds to. This
	 * method is called from the process model view that shows the bpmn via bpmn.io
	 * 
	 * @param id
	 *            of the bpmn file
	 * @return bpmn file as string
	 */
	@Path("/process-model/{id}")
	@GET
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public String getProcessModel(@RestPath Long id) {
		return usecase.getProcessModel(id);
	}

	@Path("/process-model/{id}")
	@DELETE
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public RestResponse<?> deleteProcessModel(@RestPath Long id) {
		usecase.deleteProcessModel(id);
		return ResponseBuilder.ok().build();
	}

	/**
	 * This methods gets the names and the corresponding ids of all process models
	 * in order to show them as a list in the process list in the frontend
	 */
	@GET
	@Path("/project/{projectId}/process-model")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({"User", "Admin"})
	public List<ProcessInformation> getProcessInformation(@RestPath Long projectId) {
		return usecase.getProcessInformation(projectId);
	}

	@GET
	@Path("/process-model/{id}/details")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({"User", "Admin"})
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