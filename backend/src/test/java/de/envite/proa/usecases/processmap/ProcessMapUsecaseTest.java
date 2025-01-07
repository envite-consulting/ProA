package de.envite.proa.usecases.processmap;

import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProcessMapUsecaseTest {

    private static final long PROJECT_ID = 1L;
    private static final long OLD_PROCESS_ID = 1L;
    private static final long NEW_PROCESS_ID = 2L;
    private static final long CONNECTION_ID = 1L;

    @Mock
    private ProcessMapRespository repository;

    @InjectMocks
    private ProcessMapUsecase processMapUsecase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProcessMap() {
        ProcessMap expectedProcessMap = new ProcessMap();
        when(repository.getProcessMap(PROJECT_ID)).thenReturn(expectedProcessMap);

        ProcessMap result = processMapUsecase.getProcessMap(PROJECT_ID);

        verify(repository).getProcessMap(PROJECT_ID);
        assertEquals(expectedProcessMap, result);
    }

    @Test
    void testAddConnection() {
        ProcessConnection connection = new ProcessConnection();

        processMapUsecase.addConnection(PROJECT_ID, connection);

        verify(repository).addConnection(PROJECT_ID, connection);
    }

    @Test
    void testCopyConnections() {
        processMapUsecase.copyConnections(PROJECT_ID, OLD_PROCESS_ID, NEW_PROCESS_ID);

        verify(repository).copyConnections(PROJECT_ID, OLD_PROCESS_ID, NEW_PROCESS_ID);
    }

    @Test
    void testDeleteProcessConnection() {
        processMapUsecase.deleteProcessConnection(CONNECTION_ID);

        verify(repository).deleteProcessConnection(CONNECTION_ID);
    }

    @Test
    void testDeleteDataStoreConnection() {
        processMapUsecase.deleteDataStoreConnection(CONNECTION_ID);

        verify(repository).deleteDataStoreConnection(CONNECTION_ID);
    }
}
