package de.envite.proa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.envite.proa.entities.DataAccess;
import de.envite.proa.entities.EventType;
import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessDataStore;
import de.envite.proa.entities.ProcessDetails;
import de.envite.proa.entities.ProcessElementType;
import de.envite.proa.entities.ProcessEvent;
import de.envite.proa.entities.ProcessMap;
import de.envite.proa.entities.ProcessModel;
import de.envite.proa.entities.Project;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@QuarkusTest
public class RepositoryIntegrationTest {

	private static final String DATA_STORE_LABEL = "DataStore Label";
	private static final String DATA_STORE_ID = "dataStoreId";
	private static final String EVENT_LABEL = "common event label";
	private static final String ACTIVITY_LABEL = "Activity Label";
	private static final String PROCESS_MODEL_NAME = "TestProcessModel";
	private static final String EVENT_ID = "EventID";
	private static final String ACTIVITY_ID = "ActivityId";
	private static final String PROCESS_MODEL_NAME_2 = "TestProcessModel2";
	private static final String PROJECT_NAME = "Project Name";
	private static final String PROJECT_NAME_2 = "Project Name 2";

	@Inject
	private EntityManager entityManager;

	@Inject
	private ProcessmodelRepositoryImpl processModelrepository;

	@Inject
	private ProcessMapRepositoryImpl processMapRepository;

	@Inject
	private ProjectRepositoryImpl projectRepository;

	@Test
	public void testSaveAndGetProcessModel() {

		// Arrange
		ProcessModel model = new ProcessModel();
		model.setName(PROCESS_MODEL_NAME);

		ProcessEvent startEvent = new ProcessEvent();
		startEvent.setElementId(EVENT_ID);
		startEvent.setLabel(EVENT_LABEL);
		startEvent.setEventType(EventType.START);

		model.setEvents(Arrays.asList(startEvent));

		ProcessActivity activity = new ProcessActivity();
		activity.setElementId(ACTIVITY_ID);
		activity.setLabel(ACTIVITY_LABEL);

		model.setCallActivities(Arrays.asList(activity));

		// Act
		Project project = projectRepository.createProject(PROJECT_NAME);
		Long processId = processModelrepository.saveProcessModel(project.getId(), model);
		ProcessDetails processDetails = processModelrepository.getProcessDetails(processId);

		// Assert
		assertThat(processDetails.getName()).isEqualTo(PROCESS_MODEL_NAME);
		assertThat(processDetails.getStartEvents())//
				.hasSize(1)//
				.extracting("elementId", "label", "eventType")//
				.contains(tuple(EVENT_ID, EVENT_LABEL, EventType.START));

	}

	@Test
	@Transactional
	public void testGetProcessMap() {

		// Arrange
		ProcessModel model1 = new ProcessModel();
		model1.setName(PROCESS_MODEL_NAME);

		ProcessEvent startEvent = new ProcessEvent();
		startEvent.setElementId(EVENT_ID);
		startEvent.setLabel(EVENT_LABEL);
		startEvent.setEventType(EventType.START);

		model1.setEvents(Arrays.asList(startEvent));

		ProcessModel model2 = new ProcessModel();
		model2.setName(PROCESS_MODEL_NAME_2);

		ProcessEvent endEvent = new ProcessEvent();
		endEvent.setElementId(EVENT_ID);
		endEvent.setLabel(EVENT_LABEL);
		endEvent.setEventType(EventType.END);

		model2.setEvents(Arrays.asList(endEvent));
		ProcessDataStore dataStore = new ProcessDataStore();
		dataStore.setAccess(DataAccess.READ);
		dataStore.setElementId(DATA_STORE_ID);
		dataStore.setLabel(DATA_STORE_LABEL);
		model2.setDataStores(Arrays.asList(dataStore));

		// Act
		Project project = projectRepository.createProject(PROJECT_NAME);
		Long processId1 = processModelrepository.saveProcessModel(project.getId(), model1);
		Long processId2 = processModelrepository.saveProcessModel(project.getId(), model2);

		ProcessMap processMap = processMapRepository.getProcessMap(project.getId());

		// Assert
		assertThat(processMap.getProcesses())//
				.hasSize(2)//
				.extracting("id", "name")//
				.contains(tuple(processId1, PROCESS_MODEL_NAME), //
						tuple(processId2, PROCESS_MODEL_NAME_2));

		assertThat(processMap.getConnections())//
				.hasSize(1)//
				.extracting("callingProcessid", "callingElementType", "calledProcessid", "calledElementType")//
				.contains(tuple(processId2, ProcessElementType.END_EVENT, processId1, ProcessElementType.START_EVENT));

		assertThat(processMap.getDataStores())//
				.hasSize(1)//
				.extracting("name")//
				.contains(DATA_STORE_LABEL);

		Long dataStoreId = processMap.getDataStores().get(0).getId();
		assertThat(processMap.getDataStoreConnections())//
				.hasSize(1)//
				.extracting("processid", "dataStoreId", "access")//
				.contains(tuple(processId2, dataStoreId, DataAccess.READ));
	}

	@Test
	@Transactional
	public void testDeleteProcess() {

		// Arrange
		ProcessModel model1 = new ProcessModel();
		model1.setName(PROCESS_MODEL_NAME);

		ProcessEvent startEvent = new ProcessEvent();
		startEvent.setElementId(EVENT_ID);
		startEvent.setLabel(EVENT_LABEL);
		startEvent.setEventType(EventType.START);

		model1.setEvents(Arrays.asList(startEvent));

		ProcessModel model2 = new ProcessModel();
		model2.setName(PROCESS_MODEL_NAME_2);

		ProcessEvent endEvent = new ProcessEvent();
		endEvent.setElementId(EVENT_ID);
		endEvent.setLabel(EVENT_LABEL);
		endEvent.setEventType(EventType.END);

		model2.setEvents(Arrays.asList(endEvent));
		ProcessDataStore dataStore = new ProcessDataStore();
		dataStore.setAccess(DataAccess.READ);
		dataStore.setElementId(DATA_STORE_ID);
		dataStore.setLabel(DATA_STORE_LABEL);
		model2.setDataStores(Arrays.asList(dataStore));

		// Act
		Project project = projectRepository.createProject(PROJECT_NAME);
		Long processId1 = processModelrepository.saveProcessModel(project.getId(), model1);
		Long processId2 = processModelrepository.saveProcessModel(project.getId(), model2);

		processModelrepository.deleteProcessModel(processId2);

		ProcessMap processMap = processMapRepository.getProcessMap(project.getId());

		// Assert
		assertThat(processMap.getProcesses())//
				.hasSize(1)//
				.extracting("id", "name")//
				.contains(tuple(processId1, PROCESS_MODEL_NAME));

		assertThat(processMap.getConnections()).hasSize(0);

		assertThat(processMap.getDataStores())//
				.hasSize(0);

		assertThat(processMap.getDataStoreConnections()).hasSize(0);
	}

	@Test
	@Transactional
	public void testGetProjects() {

		// Arange
		Long projectId1 = projectRepository.createProject(PROJECT_NAME).getId();
		Long projectId2 = projectRepository.createProject(PROJECT_NAME_2).getId();

		// Act
		List<Project> projects = projectRepository.getProjects();

		// Assert
		assertThat(projects)//
				.hasSize(2)//
				.extracting("id", "name")//
				.contains(tuple(projectId1, PROJECT_NAME), tuple(projectId2, PROJECT_NAME_2));

	}
	
	@Test
	@Transactional
	public void testGetProject() {

		// Arange
		Long projectId = projectRepository.createProject(PROJECT_NAME).getId();

		// Act
		Project project = projectRepository.getProject(projectId);

		// Assert
		assertThat(project.getId()).isEqualTo(projectId);
		assertThat(project.getName()).isEqualTo(PROJECT_NAME);

	}

	/**
	 * There is no quarkus feature to clean up the database
	 * https://stackoverflow.com/questions/71857904/quarkus-clean-h2-db-after-every-test
	 * https://github.com/quarkusio/quarkus/issues/14240
	 */
	@BeforeEach
	@Transactional
	public void cleanupDatabase() {
		entityManager.createNativeQuery("DELETE FROM ProcessEventTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM CallActivityTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM ProcessConnectionTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM ProcessModelTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM ProjectTable").executeUpdate();
	}
}