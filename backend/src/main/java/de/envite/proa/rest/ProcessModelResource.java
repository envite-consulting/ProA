package de.envite.proa.rest;

import de.envite.proa.dto.RelatedProcessModelRequest;
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

import java.io.File;
import java.util.List;

@Path("/api")
public class ProcessModelResource {

	private final ProcessModelUsecase usecase;
	private final FileService fileService;

	@Inject
	public ProcessModelResource(ProcessModelUsecase usecase, FileService fileService) {
		this.usecase = usecase;
		this.fileService = fileService;
	}

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
					.status(Response.Status.CREATED)
					.entity(usecase.saveProcessModel( //
							projectId, //
							fileName, //
							content, //
							description, //
							isCollaboration //
					)) //
					.build();
		} catch (CantReplaceWithCollaborationException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Path("project/{projectId}/process-model/{oldProcessId}")
	@POST
	@RolesAllowedIfWebVersion({ "User", "Admin" })
	public Response replaceProcessModel(@RestPath Long projectId, @RestPath Long oldProcessId,
										@RestForm File processModel, @RestForm String fileName,
										@RestForm String description, @RestForm boolean startProcessChangeAnalysis,
										@RestForm String geminiApiKey) {
		String content = fileService.readFileToString(processModel);
		fileName = fileName.replace(".bpmn", "");

		try {
			return Response
                    .status(Response.Status.CREATED)
                    .entity(usecase.replaceProcessModel(projectId, oldProcessId, fileName, content, description,
							startProcessChangeAnalysis, geminiApiKey))
                    .build();
		} catch (CantReplaceWithCollaborationException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

    @POST
    @Path("/project/{projectId}/process-model/{id}/related-process-model")
    @RolesAllowedIfWebVersion({"User", "Admin"})
    public Response addRelatedProcessModel(@RestPath Long projectId, Long id,
                                           RelatedProcessModelRequest request) {
        usecase.addRelatedProcessModel(projectId, id, request.getRelatedProcessModelIds());
        return Response.status(Response.Status.CREATED).build();
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
	public Response deleteProcessModel(@RestPath Long id) {
		usecase.deleteProcessModel(id);
        return Response.status(Response.Status.OK).build();
	}

	/**
	 * This method gets the names and the corresponding ids of all process models in order to show them as a list in
	 * the process list in the frontend
	 */
	@GET
	@Path("/project/{projectId}/process-model")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({ "User", "Admin" })
    public List<ProcessInformation> getProcessInformation(@RestPath Long projectId,
														  @QueryParam("levels") String levels) {
		return usecase.getProcessInformation(projectId, levels);
	}

    @GET
    @Path("/project/{projectId}/process-model/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowedIfWebVersion({ "User", "Admin" })
    public ProcessInformation getProcessInformationById(@RestPath Long projectId, Long id) {
        return usecase.getProcessInformationById(projectId, id);
    }

	@GET
	@Path("/process-model/{id}/details")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowedIfWebVersion({ "User", "Admin" })
    public ProcessDetails getProcessDetails(@RestPath Long id,
											@QueryParam("aggregate") @DefaultValue("false") boolean aggregate) {
		return usecase.getProcessDetails(id, aggregate);
	}
}
