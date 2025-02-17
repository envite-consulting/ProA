package de.envite.proa.camundacloud;

import de.envite.proa.usecases.ProcessOperations;
import de.envite.proa.usecases.processmodel.ProcessModelUsecase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class CamundaCloudImportUsecaseTest {

	private static final long PROJECT_ID = 1L;
	private static final String TEST_EMAIL = "test@example.com";
	private static final String MOCK_TOKEN = "mockToken";
	private static final String MOCK_AUTHORIZATION_HEADER = "Bearer " + MOCK_TOKEN;
	private static final String TEST_REGION_ID = "testRegion";
	private static final String TEST_CLUSTER_ID = "testCluster";
	private static final String PROCESS_ID_1 = "processId1";
	private static final String PROCESS_ID_2 = "processId2";
	private static final String XML_CONTENT_1 = "<xml></xml>";
	private static final String XML_CONTENT_2 = "<xml>...</xml>";
	private static final String TEST_PROCESS_NAME_1 = "Test Process 1";
	private static final String TEST_PROCESS_NAME_2 = "Test Process 2";
	private static final String TEST_DESCRIPTION_1 = "Test Description 1";
	private static final String TEST_DESCRIPTION_2 = "Test Description 2";
	private static final boolean IS_NOT_COLLABORATION = false;
	private static final boolean IS_COLLABORATION = true;
	private static final String IS_NOT_COLLABORATION_STRING = "false";
	private static final String IS_COLLABORATION_STRING = "true";
	private static final String NO_PARENT_BPMN_PROCESS_ID = null;
	private static final String TEST_BASE_URI = "https://test.operate.camunda.io/";

	@InjectMocks
	private CamundaCloudImportUsecase camundaCloudImportUsecase;

	@Mock
	private CamundaCloudService camundaCloudService;

	@Mock
	private CamundaModelerService camundaModelerService;

	@Mock
	private ProcessModelUsecase processModelUsecase;

	@Mock
	private ProcessOperations processOperations;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGetProcessModels() {
		CamundaCloudFetchConfiguration configuration = new CamundaCloudFetchConfiguration();
		configuration.setEmail(TEST_EMAIL);
		configuration.setToken(MOCK_TOKEN);

		ProcessSearchObject searchObject = new ProcessSearchObject();
		searchObject.getFilter().getUpdatedBy().setEmail(TEST_EMAIL);

		when(camundaModelerService.getProcessModels(MOCK_AUTHORIZATION_HEADER, searchObject))
				.thenReturn(Collections.singletonList(new Object()));

		Object result = camundaCloudImportUsecase.getProcessModels(configuration);

		verify(camundaModelerService, times(1)).getProcessModels(MOCK_AUTHORIZATION_HEADER, searchObject);
		assertEquals(1, ((List<?>) result).size());
	}

	@Test
	void testGetProcessInstances_WithoutFilter() {
		CamundaCloudFetchConfiguration configuration = new CamundaCloudFetchConfiguration();
		configuration.setRegionId(TEST_REGION_ID);
		configuration.setClusterId(TEST_CLUSTER_ID);
		configuration.setToken(MOCK_TOKEN);

		String operateUri = "https://" + //
				configuration.getRegionId() + //
				".operate.camunda.io/" + //
				configuration.getClusterId();
		ProcessInstancesFilter filter = new ProcessInstancesFilter();

		CamundaOperateService camundaOperateService = mock(CamundaOperateService.class);
		when(camundaOperateService.getProcessInstances(MOCK_AUTHORIZATION_HEADER, filter))
				.thenReturn(Collections.singletonList(new Object()));

		CamundaCloudImportUsecase usecaseSpy = spy(camundaCloudImportUsecase);
		doReturn(camundaOperateService).when(usecaseSpy).createOperateService(operateUri);

		Object result = usecaseSpy.getProcessInstances(configuration);

		verify(camundaOperateService, times(1))
				.getProcessInstances(MOCK_AUTHORIZATION_HEADER, filter);
		assertEquals(1, ((List<?>) result).size());
	}

	@Test
	void testGetProcessInstances_WithFilter() {
		CamundaCloudFetchConfiguration configuration = new CamundaCloudFetchConfiguration();
		configuration.setRegionId(TEST_REGION_ID);
		configuration.setClusterId(TEST_CLUSTER_ID);
		configuration.setToken(MOCK_TOKEN);
		configuration.setBpmnProcessId(PROCESS_ID_1);

		String operateUri = "https://" + //
				configuration.getRegionId() + //
				".operate.camunda.io/" + //
				configuration.getClusterId();
		ProcessInstancesFilter filter = new ProcessInstancesFilter(PROCESS_ID_1);

		CamundaOperateService camundaOperateService = mock(CamundaOperateService.class);
		when(camundaOperateService.getProcessInstances(MOCK_AUTHORIZATION_HEADER, filter))
				.thenReturn(Collections.singletonList(new Object()));

		CamundaCloudImportUsecase usecaseSpy = spy(camundaCloudImportUsecase);
		doReturn(camundaOperateService).when(usecaseSpy).createOperateService(operateUri);

		Object result = usecaseSpy.getProcessInstances(configuration);

		verify(camundaOperateService, times(1))
				.getProcessInstances(MOCK_AUTHORIZATION_HEADER, filter);
		assertEquals(1, ((List<?>) result).size());
	}

	@Test
	void testImportProcessModels() {
		CamundaCloudImportConfiguration config = new CamundaCloudImportConfiguration();
		config.setToken(MOCK_TOKEN);
		config.setSelectedProcessModelIds(List.of(PROCESS_ID_1, PROCESS_ID_2));

		CamundaProcessModelResponse processModel1 = new CamundaProcessModelResponse();
		processModel1.setContent(XML_CONTENT_1);
		CamundaProcessModelResponse.MetaData metadata1 = new CamundaProcessModelResponse.MetaData();
		metadata1.setName(TEST_PROCESS_NAME_1);
		processModel1.setMetadata(metadata1);

		CamundaProcessModelResponse processModel2 = new CamundaProcessModelResponse();
		processModel2.setContent(XML_CONTENT_2);
		CamundaProcessModelResponse.MetaData metadata2 = new CamundaProcessModelResponse.MetaData();
		metadata2.setName(TEST_PROCESS_NAME_2);
		processModel2.setMetadata(metadata2);

		when(camundaModelerService.getProcessModel(MOCK_AUTHORIZATION_HEADER, PROCESS_ID_1)).thenReturn(processModel1);
		when(processOperations.getDescription(XML_CONTENT_1)).thenReturn(TEST_DESCRIPTION_1);
		when(processOperations.getIsCollaboration(XML_CONTENT_1)).thenReturn(IS_NOT_COLLABORATION);

		when(camundaModelerService.getProcessModel(MOCK_AUTHORIZATION_HEADER, PROCESS_ID_2)).thenReturn(processModel2);
		when(processOperations.getDescription(XML_CONTENT_2)).thenReturn(TEST_DESCRIPTION_2);
		when(processOperations.getIsCollaboration(XML_CONTENT_2)).thenReturn(IS_COLLABORATION);

		camundaCloudImportUsecase.importProcessModels(PROJECT_ID, config);

		verify(processModelUsecase, times(1))
				.saveProcessModel(PROJECT_ID, TEST_PROCESS_NAME_1, XML_CONTENT_1, TEST_DESCRIPTION_1,
						IS_NOT_COLLABORATION_STRING, NO_PARENT_BPMN_PROCESS_ID);
		verify(processModelUsecase, times(1))
				.saveProcessModel(PROJECT_ID, TEST_PROCESS_NAME_2, XML_CONTENT_2, TEST_DESCRIPTION_2,
						IS_COLLABORATION_STRING, NO_PARENT_BPMN_PROCESS_ID);
	}

	@Test
	void testGetToken() {
		CloudCredentials credentials = new CloudCredentials();
		TokenResponse tokenResponse = new TokenResponse();
		tokenResponse.setToken(MOCK_TOKEN);

		when(camundaCloudService.getToken(credentials)).thenReturn(tokenResponse);

		String result = camundaCloudImportUsecase.getToken(credentials);

		verify(camundaCloudService, times(1)).getToken(credentials);
		assertEquals(MOCK_TOKEN, result);
	}

	@Test
	void testCreateOperateService() {
		CamundaOperateService result = camundaCloudImportUsecase.createOperateService(TEST_BASE_URI);

		assertNotNull(result, "CamundaOperateService should not be null.");
	}

}
