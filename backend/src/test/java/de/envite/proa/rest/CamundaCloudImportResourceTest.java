package de.envite.proa.rest;

import de.envite.proa.camundacloud.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CamundaCloudImportResourceTest {

    @InjectMocks
    private CamundaCloudImportResource resource;

    @Mock
    private CamundaCloudImportUsecase usecase;

    private static final String EXPECTED_TOKEN = "expectedToken";
    private static final Long PROJECT_ID = 123L;

    private static final CamundaCloudFetchConfiguration FETCH_CONFIGURATION = new CamundaCloudFetchConfiguration();
    private static final CloudCredentials CREDENTIALS = new CloudCredentials();
    private static final Object PROCESS_MODELS = new Object();
    private static final Object PROCESS_INSTANCES = new Object();
    private static final CamundaCloudImportConfiguration IMPORT_CONFIGURATION = new CamundaCloudImportConfiguration();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetToken() {
        when(usecase.getToken(CREDENTIALS)).thenReturn(EXPECTED_TOKEN);

        Object response = resource.getToken(CREDENTIALS);

        assertEquals(EXPECTED_TOKEN, response);
        verify(usecase, times(1)).getToken(CREDENTIALS);
    }

    @Test
    public void testUploadProcessModel() {
        when(usecase.getProcessModels(FETCH_CONFIGURATION)).thenReturn(PROCESS_MODELS);

        Object response = resource.uploadProcessModel(FETCH_CONFIGURATION);

        assertEquals(PROCESS_MODELS, response);
        verify(usecase, times(1)).getProcessModels(FETCH_CONFIGURATION);
    }

    @Test
    public void testGetProcessInstances() {
        when(usecase.getProcessInstances(FETCH_CONFIGURATION)).thenReturn(PROCESS_INSTANCES);

        Object response = resource.getProcessInstances(FETCH_CONFIGURATION);

        assertEquals(PROCESS_INSTANCES, response);
        verify(usecase, times(1)).getProcessInstances(FETCH_CONFIGURATION);
    }

    @Test
    public void testImportProcessModels() {
        doNothing().when(usecase).importProcessModels(PROJECT_ID, IMPORT_CONFIGURATION);

        resource.importProcessModels(PROJECT_ID, IMPORT_CONFIGURATION);

        verify(usecase, times(1)).importProcessModels(PROJECT_ID, IMPORT_CONFIGURATION);
    }
}
