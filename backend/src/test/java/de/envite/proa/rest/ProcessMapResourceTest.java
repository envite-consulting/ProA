package de.envite.proa.rest;

import de.envite.proa.entities.process.ProcessConnection;
import de.envite.proa.entities.processmap.ProcessMap;
import de.envite.proa.usecases.processmap.ProcessMapUsecase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProcessMapResourceTest {

	@InjectMocks
	private ProcessMapResource resource;

	@Mock
	private ProcessMapUsecase usecase;

	private static final Long PROJECT_ID = 1L;
	private static final Long CONNECTION_ID = 2L;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testGetProcessMap() {
		ProcessMap expectedMap = new ProcessMap();
		when(usecase.getProcessMap(PROJECT_ID)).thenReturn(expectedMap);

		ProcessMap result = resource.getProcessMap(PROJECT_ID);

		assertEquals(expectedMap, result);
		verify(usecase, times(1)).getProcessMap(PROJECT_ID);
	}

	@Test
	void testAddConnection() {
		ProcessConnection connection = new ProcessConnection();

		resource.addConnection(PROJECT_ID, connection);

		verify(usecase, times(1)).addConnection(PROJECT_ID, connection);
	}

	@Test
	void testDeleteProcessConnection() {
		resource.deleteProcessConnection(CONNECTION_ID);

		verify(usecase, times(1)).deleteProcessConnection(CONNECTION_ID);
	}

	@Test
	void testDeleteDataStoreConnection() {
		resource.deleteDataStoreConnection(CONNECTION_ID);

		verify(usecase, times(1)).deleteDataStoreConnection(CONNECTION_ID);
	}
}
