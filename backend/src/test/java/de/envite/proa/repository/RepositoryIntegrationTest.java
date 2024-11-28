package de.envite.proa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;

import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.envite.proa.entities.*;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@QuarkusTest
class RepositoryIntegrationTest {

	private static final String DATA_STORE_LABEL = "DataStore Label";
	private static final String DATA_STORE_ID = "dataStoreId";
	private static final String EVENT_LABEL = "common event label";
	private static final String ACTIVITY_LABEL = "Activity Label";
	private static final String PROCESS_MODEL_NAME = "TestProcessModel";
	private static final String EVENT_ID = "EventID";
	private static final String ACTIVITY_ID = "ActivityId";
	private static final String PROCESS_MODEL_NAME_2 = "TestProcessModel2";
	private static final String PROJECT_NAME = "Project Name";
	private static final String PROJECT_VERSION = "1.0";
	private static final String PROJECT_NAME_2 = "Project Name 2";
	private static final String PROJECT_VERSION_2 = "2.0";

	@Inject
	private EntityManager entityManager;

	@Inject
	private ProcessModelRepositoryImpl processModelRepository;

	@Inject
	private ProcessMapRepositoryImpl processMapRepository;

	@Inject
	private ProjectRepositoryImpl projectRepository;

	@Test
	void testSaveAndGetProcessModel() {

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
		Project project = projectRepository.createProject(PROJECT_NAME, PROJECT_VERSION);
		Long processId = processModelRepository.saveProcessModel(project.getId(), model);
		ProcessDetails processDetails = processModelRepository.getProcessDetails(processId);

		// Assert
		assertThat(processDetails.getName()).isEqualTo(PROCESS_MODEL_NAME);
		assertThat(processDetails.getStartEvents())//
				.hasSize(1)//
				.extracting("elementId", "label", "eventType")//
				.contains(tuple(EVENT_ID, EVENT_LABEL, EventType.START));
	}

	@Test
	@Transactional
	void testGetProcessMap() {

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
		Project project = projectRepository.createProject(PROJECT_NAME, PROJECT_VERSION);
		Long processId1 = processModelRepository.saveProcessModel(project.getId(), model1);
		Long processId2 = processModelRepository.saveProcessModel(project.getId(), model2);

		ProcessMap processMap = processMapRepository.getProcessMap(project.getId());

		// Assert
		assertThat(processMap.getProcesses())//
				.hasSize(2)//
				.extracting("id", "name")//
				.contains(tuple(processId1, PROCESS_MODEL_NAME), //
						tuple(processId2, PROCESS_MODEL_NAME_2));

		assertThat(processMap.getConnections())//
				.hasSize(1)//
				.extracting("callingProcessid", "callingElementType", "calledProcessid", "calledElementType", "label")//
				.contains(tuple(processId2, ProcessElementType.END_EVENT, processId1, ProcessElementType.START_EVENT, EVENT_LABEL));

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
	void testDeleteProcess() {

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
		Project project = projectRepository.createProject(PROJECT_NAME, PROJECT_VERSION);
		Long processId1 = processModelRepository.saveProcessModel(project.getId(), model1);
		Long processId2 = processModelRepository.saveProcessModel(project.getId(), model2);

		processModelRepository.deleteProcessModel(processId2);

		ProcessMap processMap = processMapRepository.getProcessMap(project.getId());

		// Assert
		assertThat(processMap.getProcesses())//
				.hasSize(1)//
				.extracting("id", "name")//
				.contains(tuple(processId1, PROCESS_MODEL_NAME));

		assertThat(processMap.getConnections()).isEmpty();

		assertThat(processMap.getDataStores())//
				.isEmpty();

		assertThat(processMap.getDataStoreConnections()).isEmpty();
	}

	@Test
	@Transactional
	void testGetProjects() {

		// Arrange
		Long projectId1 = projectRepository.createProject(PROJECT_NAME, PROJECT_VERSION).getId();
		Long projectId2 = projectRepository.createProject(PROJECT_NAME_2, PROJECT_VERSION_2).getId();

		// Act
		List<Project> projects = projectRepository.getProjects();

		// Assert
		assertThat(projects)//
				.hasSize(2)//
				.extracting("id", "name", "version")//
				.contains(tuple(projectId1, PROJECT_NAME, PROJECT_VERSION),
						tuple(projectId2, PROJECT_NAME_2, PROJECT_VERSION_2));
	}

	@Test
	@Transactional
	void testGetProject() {

		// Arrange
		Long projectId = projectRepository.createProject(PROJECT_NAME, PROJECT_VERSION).getId();

		// Act
		Project project = projectRepository.getProject(projectId);

		// Assert
		assertThat(project.getId()).isEqualTo(projectId);
		assertThat(project.getName()).isEqualTo(PROJECT_NAME);
		assertThat(project.getVersion()).isEqualTo(PROJECT_VERSION);
	}

	@Test
	void testDeleteProjectsWithoutUser() {

		// Arrange
		ProcessModel model = new ProcessModel();
		model.setName(PROCESS_MODEL_NAME);

		ProcessDataStore dataStore = new ProcessDataStore();
		dataStore.setAccess(DataAccess.READ);
		dataStore.setElementId(DATA_STORE_ID);
		dataStore.setLabel(DATA_STORE_LABEL);
		model.setDataStores(Arrays.asList(dataStore));

		Long projectId1 = projectRepository.createProject(PROJECT_NAME, PROJECT_VERSION).getId();
		Long projectId2 = projectRepository.createProject(PROJECT_NAME_2, PROJECT_VERSION_2).getId();

		processModelRepository.saveProcessModel(projectId1, model);

		// Act
		projectRepository.deleteProject(projectId1);
		projectRepository.deleteProject(projectId2);

		// Assert
		assertThat(processMapRepository.getProcessMap(projectId1).getDataStores()).isEmpty();
		assertThat(processMapRepository.getProcessMap(projectId1).getProcesses()).isEmpty();
		assertThat(projectRepository.getProjects()).isEmpty();
	}

	@Test
	void testDeleteProjectWithoutUserNonExistentProject() {

		// Arrange
		Long nonExistentProjectId = 1L;

		// Act & Assert
		assertThatThrownBy(() -> projectRepository.deleteProject(nonExistentProjectId))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testDeleteProjectWithUser() {

		// Arrange
		ProcessModel model = new ProcessModel();
		model.setName(PROCESS_MODEL_NAME);

		ProcessDataStore dataStore = new ProcessDataStore();
		dataStore.setAccess(DataAccess.READ);
		dataStore.setElementId(DATA_STORE_ID);
		dataStore.setLabel(DATA_STORE_LABEL);
		model.setDataStores(Arrays.asList(dataStore));

		Long userId = 1L;
		Long projectId = projectRepository.createProject(userId, PROJECT_NAME, PROJECT_VERSION).getId();

		// Act
		projectRepository.deleteProject(userId, projectId);

		// Assert
		assertThat(processMapRepository.getProcessMap(projectId).getDataStores()).isEmpty();
		assertThat(processMapRepository.getProcessMap(projectId).getProcesses()).isEmpty();
		assertThat(projectRepository.getProjects(userId)).isEmpty();
	}

	@Test
	void testDeleteProjectWithUserNonExistentProject() {

		// Arrange
		Long userId = 1L;
		Long nonExistentProjectId = 1L;

		// Act & Assert
		assertThatThrownBy(() -> projectRepository.deleteProject(userId, nonExistentProjectId))
				.isInstanceOf(NoResultException.class);
	}

	@Test
	void testDeleteProjectWithUserNotBelongingToUser() {

		// Arrange
		Long userId1 = 1L;
		Long userId2 = 2L;
		Long projectId = projectRepository.createProject(userId1, PROJECT_NAME, PROJECT_VERSION).getId();

		// Act & Assert
		assertThatThrownBy(() -> projectRepository.deleteProject(userId2, projectId))
				.isInstanceOf(NoResultException.class);
		assertThat(projectRepository.getProjects(userId1)).hasSize(1);
	}

	/**
	 * There is no quarkus feature to clean up the database
	 * https://stackoverflow.com/questions/71857904/quarkus-clean-h2-db-after-every-test
	 * https://github.com/quarkusio/quarkus/issues/14240
	 */
	@BeforeEach
	@Transactional
	void cleanupDatabase() {
		entityManager.createNativeQuery("DELETE FROM ProcessEventTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM CallActivityTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM ProcessConnectionTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM ProcessModelTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM ProjectTable").executeUpdate();
	}
}
