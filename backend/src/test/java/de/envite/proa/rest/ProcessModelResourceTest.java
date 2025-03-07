package de.envite.proa.rest;

import de.envite.proa.dto.RelatedProcessModelRequest;
import de.envite.proa.entities.process.ProcessDetails;
import de.envite.proa.entities.process.ProcessInformation;
import de.envite.proa.usecases.processmodel.ProcessModelUsecase;
import de.envite.proa.usecases.processmodel.exceptions.CantReplaceWithCollaborationException;
import jakarta.ws.rs.core.Response;
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

class ProcessModelResourceTest {

	private static final String PROCESS_XML = "ProcessXML";
	private static final String FILE_NAME = "testfile.bpmn";
	private static final String FILE_NAME_TRIMMED = FILE_NAME.replace(".bpmn", "");
	private static final String DESCRIPTION = "description";
	private static final Long PROJECT_ID = 1L;
	private static final Long PROCESS_ID = 54321L;
	private static final boolean IS_COLLABORATION = true;
	private static final Long NEW_PROCESS_ID = 123L;
	private static final String TEST_DIAGRAM = "test-diagram.bpmn";

	@Mock
	private ProcessModelUsecase usecase;

	@Mock
	private FileService fileService;

	@InjectMocks
	ProcessModelResource resource;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testUploadProcessModel()
			throws CantReplaceWithCollaborationException {
		File processModel = new File(Objects.requireNonNull( //
				getClass().getClassLoader().getResource(TEST_DIAGRAM)).getFile());
		when(fileService.readFileToString(processModel)).thenReturn(PROCESS_XML);

		when(usecase.saveProcessModel(PROJECT_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION,
				IS_COLLABORATION)).thenReturn(PROCESS_ID);

		Response response = resource.uploadProcessModel(PROJECT_ID, processModel, FILE_NAME, //
				DESCRIPTION, IS_COLLABORATION);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo(201);
		assertThat(response.getEntity()).isInstanceOf(Long.class);
		assertThat((Long) response.getEntity()).isEqualTo(PROCESS_ID);

		verify(fileService).readFileToString(processModel);
		verify(usecase).saveProcessModel(PROJECT_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION, IS_COLLABORATION);
	}

	@Test
	void testUploadProcessModel_InternalError()
			throws CantReplaceWithCollaborationException {
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
	void testAddRelatedProcessModel() {
		RelatedProcessModelRequest request = new RelatedProcessModelRequest();
		request.setRelatedProcessModelIds(List.of(1L, 2L));

		try (Response response = resource.addRelatedProcessModel(PROJECT_ID, PROCESS_ID, request)) {
			assertThat(response.getStatus()).isEqualTo(201);
		}

		verify(usecase).addRelatedProcessModel(PROJECT_ID, PROCESS_ID, request.getRelatedProcessModelIds());
	}

	@Test
	void testGetProcessModel() {
		when(usecase.getProcessModel(PROCESS_ID)).thenReturn(PROCESS_XML);

		String result = resource.getProcessModel(PROCESS_ID);

		assertThat(result).isEqualTo(PROCESS_XML);

		verify(usecase).getProcessModel(PROCESS_ID);
	}

	@Test
	void testDeleteProcessModel() {
		doNothing().when(usecase).deleteProcessModel(PROCESS_ID);

		try (Response response = resource.deleteProcessModel(PROCESS_ID)) {
			assertThat(response.getStatus()).isEqualTo(200);
			assertThat(response.getEntity()).isNull();
		}

		verify(usecase).deleteProcessModel(PROCESS_ID);
	}

	@Test
	void testGetProcessInformation() {
		List<ProcessInformation> expectedResultList = List.of(new ProcessInformation());

		when(usecase.getProcessInformation(PROJECT_ID, null)).thenReturn(expectedResultList);

		List<ProcessInformation> result = resource.getProcessInformation(PROJECT_ID, null);

		assertThat(result).isEqualTo(expectedResultList);

		verify(usecase).getProcessInformation(PROJECT_ID, null);
	}

	@Test
	void testGetProcessInformationWithLevels() {
		String levels = "1,2";
		List<ProcessInformation> expectedResultList = List.of(new ProcessInformation());

		when(usecase.getProcessInformation(PROJECT_ID, levels)).thenReturn(expectedResultList);

		List<ProcessInformation> result = resource.getProcessInformation(PROJECT_ID, levels);

		assertThat(result).isEqualTo(expectedResultList);

		verify(usecase).getProcessInformation(PROJECT_ID, levels);
	}

	@Test
	void testGetProcessInformationById() {
		ProcessInformation expectedProcess = new ProcessInformation();

		when(usecase.getProcessInformationById(PROJECT_ID, PROCESS_ID)).thenReturn(expectedProcess);

		ProcessInformation result = resource.getProcessInformationById(PROJECT_ID, PROCESS_ID);

		assertThat(result).isEqualTo(expectedProcess);

		verify(usecase).getProcessInformationById(PROJECT_ID, PROCESS_ID);
	}

	@Test
	void testGetProcessDetails() {
		ProcessDetails expected = new ProcessDetails();

		when(usecase.getProcessDetails(PROCESS_ID, false)).thenReturn(expected);

		ProcessDetails result = resource.getProcessDetails(PROCESS_ID, false);

		assertThat(result).isEqualTo(expected);
		verify(usecase).getProcessDetails(PROCESS_ID, false);
	}

	@Test
	void testReplaceProcessModel() throws CantReplaceWithCollaborationException {
		File processModel = new File(
				Objects.requireNonNull(getClass().getClassLoader().getResource(TEST_DIAGRAM)).getFile());

		when(fileService.readFileToString(processModel)).thenReturn(PROCESS_XML);
		when(usecase.replaceProcessModel(PROJECT_ID, PROCESS_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION, false, null))
				.thenReturn(NEW_PROCESS_ID);

		try (Response response = resource.replaceProcessModel(PROJECT_ID, PROCESS_ID, processModel, FILE_NAME, DESCRIPTION, false, null)) {
			Long returnedProcessId = (Long) response.getEntity();

			assertThat(response.getStatus()).isEqualTo(201);
			assertThat(returnedProcessId).isEqualTo(NEW_PROCESS_ID);
		}

		verify(fileService).readFileToString(processModel);
		verify(usecase).replaceProcessModel(PROJECT_ID, PROCESS_ID, FILE_NAME_TRIMMED, PROCESS_XML, DESCRIPTION, false, null);
	}
}
