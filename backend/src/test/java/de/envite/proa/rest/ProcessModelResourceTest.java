package de.envite.proa.rest;

import de.envite.proa.entities.process.ProcessDetails;
import de.envite.proa.entities.process.ProcessInformation;
import de.envite.proa.usecases.processmodel.ProcessModelUsecase;
import de.envite.proa.usecases.processmodel.exceptions.CantReplaceWithCollaborationException;
import de.envite.proa.usecases.processmodel.exceptions.CollaborationAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ProcessModelResourceTest {

	private static final String PROCESS_XML = "ProcessXML";
	private static final String FILE_NAME = "testfile.bpmn";
	private static final String FILE_NAME_TRIMMED = FILE_NAME.replace(".bpmn", "");
	private static final String DESCRIPTION = "description";
	private static final Long PROJECT_ID = 1L;
	private static final Long PROCESS_ID = 54321L;
	private static final boolean IS_COLLABORATION = true;
	private static final String COLLABORATION_NAME = "collaboration123";
	private static final Long NEW_PROCESS_ID = 123L;
	private static final String TEST_DIAGRAM = "test-diagram.bpmn";
	private static final String BPMN_ID = "bpmnId";

	@Mock
	private ProcessModelUsecase usecase;

	@Mock
	private FileService fileService;

	@InjectMocks
	ProcessModelResource resource;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testUploadProcessModel()
			throws CollaborationAlreadyExistsException, CantReplaceWithCollaborationException {
		File processModel = new File(Objects.requireNonNull( //
				getClass().getClassLoader().getResource(TEST_DIAGRAM)).getFile());
		when(fileService.readFileToString(processModel)).thenReturn(PROCESS_XML);

		when(usecase.saveProcessModel(PROJECT_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION,
				IS_COLLABORATION)).thenReturn(PROCESS_ID);

		Response response = resource.uploadProcessModel(PROJECT_ID, processModel, FILE_NAME, //
				DESCRIPTION, IS_COLLABORATION);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getEntity()).isInstanceOf(Long.class);
		assertThat((Long) response.getEntity()).isEqualTo(PROCESS_ID);

		verify(fileService).readFileToString(processModel);
		verify(usecase).saveProcessModel(PROJECT_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION, IS_COLLABORATION);
	}

	@Test
	public void testUploadProcessModel_InternalError()
			throws CollaborationAlreadyExistsException, CantReplaceWithCollaborationException {
		File processModel = new File(Objects.requireNonNull( //
				getClass().getClassLoader().getResource(TEST_DIAGRAM)).getFile());
		when(fileService.readFileToString(processModel)).thenReturn(PROCESS_XML);

		doThrow(RuntimeException.class).when(usecase).saveProcessModel(PROJECT_ID, FILE_NAME_TRIMMED, PROCESS_XML,
				DESCRIPTION, IS_COLLABORATION);

		Response response = resource.uploadProcessModel(PROJECT_ID, processModel, FILE_NAME, //
				DESCRIPTION, IS_COLLABORATION);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(500);
		assertThat(response.getEntity()).isNull();

		verify(fileService).readFileToString(processModel);
		verify(usecase).saveProcessModel(PROJECT_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION, IS_COLLABORATION);
	}

	@Test
	public void testGetProcessModel() {
		when(usecase.getProcessModel(PROCESS_ID)).thenReturn(PROCESS_XML);

		String result = resource.getProcessModel(PROCESS_ID);

		assertThat(result).isEqualTo(PROCESS_XML);

		verify(usecase).getProcessModel(PROCESS_ID);
	}

	@Test
	public void testDeleteProcessModel() {
		doNothing().when(usecase).deleteProcessModel(PROCESS_ID);

		RestResponse<?> response = resource.deleteProcessModel(PROCESS_ID);

		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getEntity()).isNull();

		verify(usecase).deleteProcessModel(PROCESS_ID);
	}

	@Test
	public void testGetProcessInformation() {
		List<ProcessInformation> expectedResultList = List.of(new ProcessInformation());

		when(usecase.getProcessInformation(PROJECT_ID)).thenReturn(expectedResultList);

		List<ProcessInformation> result = resource.getProcessInformation(PROJECT_ID);

		assertThat(result).isEqualTo(expectedResultList);

		verify(usecase).getProcessInformation(PROJECT_ID);
	}

	@Test
	public void testGetProcessDetails() {
		ProcessDetails expected = new ProcessDetails();

		when(usecase.getProcessDetails(PROCESS_ID)).thenReturn(expected);

		ProcessDetails result = resource.getProcessDetails(PROCESS_ID);

		assertThat(result).isEqualTo(expected);
		verify(usecase).getProcessDetails(PROCESS_ID);
	}

	@Test
	public void testUploadProcessModel_CollaborationAlreadyExistsException()
			throws CollaborationAlreadyExistsException, CantReplaceWithCollaborationException {
		File processModel = new File(
				Objects.requireNonNull(getClass().getClassLoader().getResource("test-diagram.bpmn")).getFile());

		when(fileService.readFileToString(processModel)).thenReturn(PROCESS_XML);
		when(usecase.saveProcessModel(PROJECT_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION, IS_COLLABORATION))
				.thenThrow(new CollaborationAlreadyExistsException(BPMN_ID, COLLABORATION_NAME));

		Response response = resource.uploadProcessModel(PROJECT_ID, processModel, FILE_NAME, DESCRIPTION,
				IS_COLLABORATION);

		verify(usecase).saveProcessModel(PROJECT_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION, IS_COLLABORATION);

		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
		CollaborationAlreadyExistsException e = (CollaborationAlreadyExistsException) response.getEntity();
		assertThat(e.getExceptionType()).isEqualTo("CollaborationAlreadyExistsException");
		assertThat(e.getName()).isEqualTo(COLLABORATION_NAME);
		assertThat(e.getMessage()).contains(BPMN_ID);
	}

	@Test
	public void testReplaceProcessModel() throws CantReplaceWithCollaborationException {
		File processModel = new File(
				Objects.requireNonNull(getClass().getClassLoader().getResource("test-diagram.bpmn")).getFile());

		when(fileService.readFileToString(processModel)).thenReturn(PROCESS_XML);
		when(usecase.replaceProcessModel(PROJECT_ID, PROCESS_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION))
				.thenReturn(NEW_PROCESS_ID);

		Response response = resource.replaceProcessModel(PROJECT_ID, PROCESS_ID, processModel, FILE_NAME, DESCRIPTION);

		assertThat(response.getEntity()).isEqualTo(NEW_PROCESS_ID);

		verify(fileService).readFileToString(processModel);
		verify(usecase).replaceProcessModel(PROJECT_ID, PROCESS_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION);
	}
}
