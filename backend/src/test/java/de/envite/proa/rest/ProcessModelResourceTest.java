package de.envite.proa.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.usecases.ProcessModelUsecase;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ProcessModelResourceTest {

	private static final String PROCESS_XML = "ProcessXML";
	private static final String FILE_NAME = "testfile.bpmn";
	private static final String FILE_NAME_TRIMMED = FILE_NAME.replace(".bpmn", "");
	private static final String DESCRIPTION = "description";
	private static final Long PROJECT_ID = 1L;
	private static final Long PROCESS_ID = 54321L;
	private static final int PROCESS_ID_AS_INT = Math.toIntExact(PROCESS_ID);
	private static final String IS_COLLABORATION = "false";
	private static final String IS_COLLABORATION_TRUE = "true";
	private static final String PARENT_BPMN_PROCESS_ID = null;
	private static final String COLLABORATION_NAME = "collaboration123";
	private static final String COLLABORATION_EXISTS_ERROR_MESSAGE =
			"Collaboration already exists: " + COLLABORATION_NAME;
	private static final Long NEW_PROCESS_ID = 123L;

	@InjectMock
	private ProcessModelUsecase usecase;

	@InjectMock
	private FileService fileService;

	@Inject
	ProcessModelResource resource;

	@Test
	public void testUploadProcessModel() throws IOException {

		// Arrange
		File processModel = new File(getClass().getClassLoader().getResource("test-diagram.bpmn").getFile());
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());
		when(fileService.readFileToString(any(File.class))).thenReturn(bpmnXml);

		// Act
		given()//
				.multiPart("processModel", processModel)//
				.multiPart("fileName", FILE_NAME)//
				.multiPart(DESCRIPTION, DESCRIPTION)//
				.multiPart("isCollaboration", IS_COLLABORATION)//
				.when()//
				.post("/api/project/" + PROJECT_ID + "/process-model")//
				.then()//
				.statusCode(200);

		// Assert
		ArgumentCaptor<Long> projectIdCaptor = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> isCollaborationCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> parentBpmnProcessIdCaptor = ArgumentCaptor.forClass(String.class);
		verify(usecase).saveProcessModel(projectIdCaptor.capture(), fileNameCaptor.capture(), contentCaptor.capture(),
				descriptionCaptor.capture(), isCollaborationCaptor.capture(), parentBpmnProcessIdCaptor.capture());
		verify(fileService).readFileToString(any(File.class));

		assertThat(projectIdCaptor.getValue()).isEqualTo(PROJECT_ID);
		assertThat(fileNameCaptor.getValue()).isEqualTo(FILE_NAME.replace(".bpmn", ""));
		assertThat(contentCaptor.getValue()).isEqualTo(bpmnXml);
		assertThat(descriptionCaptor.getValue()).isEqualTo(DESCRIPTION);
		assertThat(isCollaborationCaptor.getValue()).isEqualTo(IS_COLLABORATION);
	}

	@Test
	public void testGetProcessModel() {

		// Arrange
		when(usecase.getProcessModel(PROCESS_ID)).thenReturn(PROCESS_XML);

		// Act
		given()//
				.when()//
				.get("/api/process-model/" + PROCESS_ID)//
				.then()//
				.body(equalTo(PROCESS_XML))//
				.statusCode(200);
	}

	@Test
	public void testDelete() {
		// Act
		given()//
				.when()//
				.delete("/api/process-model/" + PROCESS_ID)//
				.then()//
				.statusCode(200);

		// Assert
		ArgumentCaptor<Long> processIdCaptor = ArgumentCaptor.forClass(Long.class);
		verify(usecase).deleteProcessModel(processIdCaptor.capture());

		assertThat(processIdCaptor.getValue()).isEqualTo(PROCESS_ID);
	}

	@Test
	public void testGetProcessInformation() {

		// Arrange
		ProcessInformation information = new ProcessInformation();
		information.setId(PROCESS_ID);
		when(usecase.getProcessInformation(PROJECT_ID)).thenReturn(Arrays.asList(information));

		// Act
		given()//
				.when()//
				.get("/api/project/" + PROJECT_ID + "/process-model")//
				.then()//
				.body("id", hasItems(PROCESS_ID_AS_INT))//
				.statusCode(200);
	}

	@Test
	public void testGetProcessDetails() {

		// Arrange
		ProcessDetails details = new ProcessDetails();
		details.setName(FILE_NAME);
		when(usecase.getProcessDetails(PROCESS_ID)).thenReturn(details);

		// Act
		given()//
				.when()//
				.get("/api/process-model/" + PROCESS_ID + "/details")//
				.then()//
				.body("name", is(FILE_NAME))//
				.statusCode(200);
	}

	@Test
	public void testUploadProcessModelIllegalArgument() {
		File processModel = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("test-diagram.bpmn")).getFile());

		when(fileService.readFileToString(processModel)).thenReturn(PROCESS_XML);
		when(usecase.saveProcessModel(PROJECT_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION, IS_COLLABORATION_TRUE, PARENT_BPMN_PROCESS_ID))
				.thenThrow(new IllegalArgumentException(COLLABORATION_EXISTS_ERROR_MESSAGE));

		Response response = resource.uploadProcessModel(PROJECT_ID, processModel, FILE_NAME, DESCRIPTION, IS_COLLABORATION_TRUE);

		verify(usecase).saveProcessModel(PROJECT_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION, IS_COLLABORATION_TRUE, PARENT_BPMN_PROCESS_ID);

		assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
		@SuppressWarnings("unchecked")
		Map<String, String> entity = (Map<String, String>) response.getEntity();
		assertThat(entity).containsKeys("error", "data");
		assertThat(entity.get("error")).isEqualTo(COLLABORATION_EXISTS_ERROR_MESSAGE);
		assertThat(entity.get("data")).isEqualTo(COLLABORATION_NAME);
	}

	@Test
	public void testReplaceProcessModel() {
		File processModel = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("test-diagram.bpmn")).getFile());

		when(fileService.readFileToString(any(File.class))).thenReturn(PROCESS_XML);
		when(usecase.replaceProcessModel(PROJECT_ID, PROCESS_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION, PARENT_BPMN_PROCESS_ID))
				.thenReturn(NEW_PROCESS_ID);

		Long response = resource.replaceProcessModel(PROJECT_ID, PROCESS_ID, processModel, FILE_NAME, DESCRIPTION);

		assertThat(response).isEqualTo(NEW_PROCESS_ID);

		verify(fileService).readFileToString(any(File.class));
		verify(usecase).replaceProcessModel(PROJECT_ID, PROCESS_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION, PARENT_BPMN_PROCESS_ID);
	}
}