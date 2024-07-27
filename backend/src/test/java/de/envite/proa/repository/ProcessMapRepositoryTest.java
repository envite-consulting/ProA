package de.envite.proa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.envite.proa.entities.DataAccess;
import de.envite.proa.entities.ProcessElementType;
import de.envite.proa.entities.ProcessMap;
import de.envite.proa.repository.tables.DataStoreConnectionTable;
import de.envite.proa.repository.tables.DataStoreTable;
import de.envite.proa.repository.tables.ProcessConnectionTable;
import de.envite.proa.repository.tables.ProcessModelTable;

public class ProcessMapRepositoryTest {

	private static final Long PROCESS_MODEL_ID_1 = 1L;
	private static final String PROCESS_MODEL_NAME_1 = "processModelName_1";
	private static final String PROCESS_DESCRIPTION_1 = "processDescription_1";

	private static final Long PROCESS_MODEL_ID_2 = 2L;
	private static final String PROCESS_MODEL_NAME_2 = "processModelName_2";
	private static final String PROCESS_DESCRIPTION_2 = "processDescription_2";

	private static final String CALLING_ELEMENT_ID = "callingElementId";
	private static final String CALLED_ELEMENT_ID = "calledElementId";
	
	private static final String COMMON_LABEL = "event label";

	private static final String DATA_STORE_LABEL = "dataStoreLabel";
	private static final Long DATA_STORE_ID = 3L;

	@Mock
	private ProjectDao projectDao;
	@Mock
	private ProcessModelDao processModelDao;
	@Mock
	private ProcessConnectionDao processConnectionDao;
	@Mock
	private DataStoreDao dataStoreDao;
	@Mock
	private DataStoreConnectionDao dataStoreConnectionDao;
	@Mock
	private CallActivityDao callActivityDao;
	@Mock
	private ProcessEventDao processEventDao;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetProcessMap() {
		// Arrange

		ProcessMapRepositoryImpl repository = new ProcessMapRepositoryImpl(//
				projectDao,
				processModelDao, //
				processConnectionDao, //
				dataStoreDao, //
				dataStoreConnectionDao, //
				callActivityDao, //
				processEventDao);

		ProcessModelTable processModel1 = new ProcessModelTable();
		processModel1.setId(PROCESS_MODEL_ID_1);
		processModel1.setName(PROCESS_MODEL_NAME_1);
		processModel1.setDescription(PROCESS_DESCRIPTION_1);

		ProcessModelTable processModel2 = new ProcessModelTable();
		processModel2.setId(PROCESS_MODEL_ID_2);
		processModel2.setName(PROCESS_MODEL_NAME_2);
		processModel2.setDescription(PROCESS_DESCRIPTION_2);

		when(processModelDao.getProcessModels(any())).thenReturn(Arrays.asList(processModel1, processModel2));

		ProcessConnectionTable processConnectionTable = new ProcessConnectionTable();
		processConnectionTable.setCallingProcess(processModel1);
		processConnectionTable.setCallingElement(CALLING_ELEMENT_ID);
		processConnectionTable.setCallingElementType(ProcessElementType.END_EVENT);
		processConnectionTable.setCalledProcess(processModel2);
		processConnectionTable.setCalledElement(CALLED_ELEMENT_ID);
		processConnectionTable.setCalledElementType(ProcessElementType.START_EVENT);

		when(processConnectionDao.getProcessConnections(any())).thenReturn(List.of(processConnectionTable));
		processConnectionTable.setLabel(COMMON_LABEL);

		DataStoreTable dataStoreTable = new DataStoreTable();
		dataStoreTable.setLabel(DATA_STORE_LABEL);
		dataStoreTable.setId(DATA_STORE_ID);
		when(dataStoreDao.getDataStores(any())).thenReturn(List.of(dataStoreTable));

		DataStoreConnectionTable dataStoreConnectionTable = new DataStoreConnectionTable();
		dataStoreConnectionTable.setProcess(processModel1);
		dataStoreConnectionTable.setDataStore(dataStoreTable);
		dataStoreConnectionTable.setAccess(DataAccess.READ_WRITE);
		when(dataStoreConnectionDao.getDataStoreConnections(any())).thenReturn(List.of(dataStoreConnectionTable));

		// Act
		ProcessMap processMap = repository.getProcessMap(anyLong());

		// Assert
		assertThat(processMap.getProcesses())//
				.hasSize(2)//
				.extracting("id", "name", "description")//
				.contains(//
						tuple(PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, PROCESS_DESCRIPTION_1), //
						tuple(PROCESS_MODEL_ID_2, PROCESS_MODEL_NAME_2, PROCESS_DESCRIPTION_2));

		assertThat(processMap.getConnections())//
				.hasSize(1)//
				.extracting("callingProcessid", "callingElementType", "calledProcessid", "calledElementType", "label")//
				.contains(tuple(PROCESS_MODEL_ID_1, ProcessElementType.END_EVENT, PROCESS_MODEL_ID_2,
						ProcessElementType.START_EVENT, COMMON_LABEL));

		assertThat(processMap.getDataStores())//
				.hasSize(1)//
				.extracting("id", "name")//
				.contains(tuple(DATA_STORE_ID, DATA_STORE_LABEL));

		assertThat(processMap.getDataStoreConnections())//
				.hasSize(1)//
				.extracting("processid", "dataStoreId", "access")//
				.contains(tuple(PROCESS_MODEL_ID_1, DATA_STORE_ID, DataAccess.READ_WRITE));
	}
}