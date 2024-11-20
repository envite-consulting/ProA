package de.envite.proa.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessInformation;
import de.envite.proa.usecases.processmodel.ProcessModelUsecase;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ProcessModelResourceTest {

	private static final String PROCESS_XML = "ProcessXML";
	private static final String FILE_NAME = "testfile.bpmn";
	private static final String DESCRIPTION = "description";
	private static final Long PROJECT_ID = 1L;
	private static final Long PROCESS_ID = 54321L;
	private static final int PROCESS_ID_AS_INT = Math.toIntExact(PROCESS_ID);
	private static final String IS_COLLABORATION = "false";
	private static final String PARENT_BPMN_PROCESS_ID = null;

	@InjectMock
	private ProcessModelUsecase usecase;

	@Test
	public void testUploadProcessModel() throws IOException {

		// Arrange
		File processModel = new File(getClass().getClassLoader().getResource("test-diagram.bpmn").getFile());
		String bpmnXml = new String(
				getClass().getClassLoader().getResourceAsStream("test-diagram.bpmn").readAllBytes());

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
}