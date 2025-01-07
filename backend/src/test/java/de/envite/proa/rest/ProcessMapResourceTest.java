package de.envite.proa.rest;

import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessMap;
import de.envite.proa.usecases.processmap.ProcessMapUsecase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessMapResourceTest {

    @InjectMocks
    private ProcessMapResource resource;

    @Mock
    private ProcessMapUsecase usecase;

    private static final Long PROJECT_ID = 1L;
    private static final Long CONNECTION_ID = 2L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetProcessMap() {
        ProcessMap expectedMap = new ProcessMap();
        when(usecase.getProcessMap(PROJECT_ID)).thenReturn(expectedMap);

        ProcessMap result = resource.getProcessMap(PROJECT_ID);

        assertEquals(expectedMap, result);
        verify(usecase, times(1)).getProcessMap(PROJECT_ID);
    }

    @Test
    public void testAddConnection() {
        ProcessConnection connection = new ProcessConnection();

        resource.addConnection(PROJECT_ID, connection);

        verify(usecase, times(1)).addConnection(PROJECT_ID, connection);
    }

    @Test
    public void testDeleteProcessConnection() {
        resource.deleteProcessConnection(CONNECTION_ID);

        verify(usecase, times(1)).deleteProcessConnection(CONNECTION_ID);
    }

    @Test
    public void testDeleteDataStoreConnection() {
        resource.deleteDataStoreConnection(CONNECTION_ID);

        verify(usecase, times(1)).deleteDataStoreConnection(CONNECTION_ID);
    }
}
