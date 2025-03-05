package de.envite.proa.rest;

import de.envite.proa.entities.process.ProcessDetails;
import de.envite.proa.entities.process.ProcessInformation;
import de.envite.proa.security.RolesAllowedIfWebVersion;
import de.envite.proa.usecases.processmodel.ProcessModelUsecase;
import de.envite.proa.usecases.processmodel.exceptions.CantReplaceWithCollaborationException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

import java.io.File;
import java.util.List;

@Path("/api")
public class ProcessModelResource {

	@Inject
	private ProcessModelUsecase usecase;

	@Inject
	FileService fileService;

	/**
	 * Creates a new process model
	 *
	 * @param projectId
	 * 		the id of the project the process model belongs to
	 * @param processModel
	 * 		the bpmn file
	 * @param fileName
	 * 		the file name of the bpmn file
	 * @param description
	 * 		the description of the process
	 * @return id of saved process model
	 */
	@POST
	@Path("/project/{projectId}/process-model")
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public Response uploadProcessModel(@RestPath Long projectId, @RestForm File processModel, @RestForm String fileName,
			@RestForm String description, @RestForm boolean isCollaboration) {
		try {
			String content = fileService.readFileToString(processModel);
			fileName = fileName.replace(".bpmn", "");
			return Response //
					.ok(usecase.saveProcessModel( //
							projectId, //
							fileName, //
							content, //
							description, //
							isCollaboration //
					)) //
					.build();
		} catch (CantReplaceWithCollaborationException e) {
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("project/{projectId}/process-model/{oldProcessId}")
	@POST
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public Response replaceProcessModel(@RestPath Long projectId, @RestPath Long oldProcessId,
			@RestForm File processModel,
			@RestForm String fileName, @RestForm String description) {
		String content = fileService.readFileToString(processModel);
		fileName = fileName.replace(".bpmn", "");
		try {
			Long id = usecase.replaceProcessModel(projectId, oldProcessId, fileName, content, description);
			return Response.ok(id).build();
		} catch (CantReplaceWithCollaborationException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Gets the xml representations of the bpmn file the id corresponds to. This method is called from the process model
	 * view that shows the bpmn via bpmn.io
	 *
	 * @param id
	 * 		of the bpmn file
	 * @return bpmn file as string
	 */
	@Path("/process-model/{id}")
	@GET
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public String getProcessModel(@RestPath Long id) {
		return usecase.getProcessModel(id);
	}

	@Path("/process-model/{id}")
	@DELETE
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public RestResponse<?> deleteProcessModel(@RestPath Long id) {
		usecase.deleteProcessModel(id);
		return ResponseBuilder.ok().build();
	}

	/**
	 * This methods gets the names and the corresponding ids of all process models in order to show them as a list in
	 * the process list in the frontend
	 */
	@GET
	@Path("/project/{projectId}/process-model")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public List<ProcessInformation> getProcessInformation(@RestPath Long projectId) {
		return usecase.getProcessInformation(projectId);
	}

	@GET
	@Path("/process-model/{id}/details")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public ProcessDetails getProcessDetails(@RestPath Long id) {
		return usecase.getProcessDetails(id);
	}
}