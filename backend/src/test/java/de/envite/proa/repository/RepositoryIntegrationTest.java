package de.envite.proa.repository;

import de.envite.proa.entities.authentication.User;
import de.envite.proa.entities.datastore.DataAccess;
import de.envite.proa.entities.process.*;
import de.envite.proa.entities.processmap.ProcessMap;
import de.envite.proa.entities.project.Project;
import de.envite.proa.repository.authentication.AuthenticationRepositoryImpl;
import de.envite.proa.repository.processmap.ProcessMapRepositoryImpl;
import de.envite.proa.repository.processmodel.ProcessmodelRepositoryImpl;
import de.envite.proa.repository.project.ProjectRepositoryImpl;
import de.envite.proa.repository.user.UserRepositoryImpl;
import de.envite.proa.usecases.authentication.exceptions.EmailAlreadyRegisteredException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
class RepositoryIntegrationTest {
	private static final String USER_EMAIL_1 = "email1@example.com";
	private static final String USER_EMAIL_2 = "email2@example.com";
	private static final String USER_PASSWORD = "P@ssword123";
	private static final String USER_ROLE = "User";
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
	UserRepositoryImpl userRepositoryImpl;

	@Inject
	private EntityManager entityManager;

	@Inject
	private ProcessmodelRepositoryImpl processModelRepository;

	@Inject
	private ProcessMapRepositoryImpl processMapRepository;

	@Inject
	private ProjectRepositoryImpl projectRepository;

	@Inject
	private AuthenticationRepositoryImpl authenticationRepository;

	@Test
	void testSaveAndGetProcessModel() {

		// Arrange
		ProcessModel model = new ProcessModel();
		model.setName(PROCESS_MODEL_NAME);

		ProcessEvent startEvent = new ProcessEvent();
		startEvent.setElementId(EVENT_ID);
		startEvent.setLabel(EVENT_LABEL);
		startEvent.setEventType(EventType.START);
		model.setEvents(Set.of(startEvent));

		ProcessActivity activity = new ProcessActivity();
		activity.setElementId(ACTIVITY_ID);
		activity.setLabel(ACTIVITY_LABEL);
		model.setCallActivities(Set.of(activity));

		// Act
		Project project = projectRepository.createProject(PROJECT_NAME, PROJECT_VERSION);
		Long processId = processModelRepository.saveProcessModel(project.getId(), model);
		ProcessDetails processDetails = processModelRepository.getProcessDetails(processId, false);

		// Assert
		assertThat(processDetails.getName()).isEqualTo(PROCESS_MODEL_NAME);
		assertThat(processDetails.getStartEvents())//
				.hasSize(1)//
				.extracting("elementId", "label", "eventType")//
				.contains(tuple(EVENT_ID, EVENT_LABEL, EventType.START));
	}

	@Test
	void testGetProcessMap() {

		// Arrange
		ProcessModel model1 = new ProcessModel();
		model1.setName(PROCESS_MODEL_NAME);

		ProcessEvent startEvent = new ProcessEvent();
		startEvent.setElementId(EVENT_ID);
		startEvent.setLabel(EVENT_LABEL);
		startEvent.setEventType(EventType.START);
		model1.setEvents(Set.of(startEvent));

		ProcessModel model2 = new ProcessModel();
		model2.setName(PROCESS_MODEL_NAME_2);

		ProcessEvent endEvent = new ProcessEvent();
		endEvent.setElementId(EVENT_ID);
		endEvent.setLabel(EVENT_LABEL);
		endEvent.setEventType(EventType.END);
		model2.setEvents(Set.of(endEvent));

		ProcessDataStore dataStore = new ProcessDataStore();
		dataStore.setAccess(DataAccess.READ);
		dataStore.setElementId(DATA_STORE_ID);
		dataStore.setLabel(DATA_STORE_LABEL);
		model2.setDataStores(Collections.singletonList(dataStore));

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
				.contains(tuple(processId2, ProcessElementType.END_EVENT, processId1, ProcessElementType.START_EVENT,
						EVENT_LABEL));

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
	void testDeleteProcess() {

		// Arrange
		ProcessModel model1 = new ProcessModel();
		model1.setName(PROCESS_MODEL_NAME);

		ProcessEvent startEvent = new ProcessEvent();
		startEvent.setElementId(EVENT_ID);
		startEvent.setLabel(EVENT_LABEL);
		startEvent.setEventType(EventType.START);
		model1.setEvents(Set.of(startEvent));

		ProcessModel model2 = new ProcessModel();
		model2.setName(PROCESS_MODEL_NAME_2);

		ProcessEvent endEvent = new ProcessEvent();
		endEvent.setElementId(EVENT_ID);
		endEvent.setLabel(EVENT_LABEL);
		endEvent.setEventType(EventType.END);
		model2.setEvents(Set.of(endEvent));

		ProcessDataStore dataStore = new ProcessDataStore();
		dataStore.setAccess(DataAccess.READ);
		dataStore.setElementId(DATA_STORE_ID);
		dataStore.setLabel(DATA_STORE_LABEL);
		model2.setDataStores(Collections.singletonList(dataStore));

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
		model.setDataStores(Collections.singletonList(dataStore));

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
	void testDeleteProjectWithUser() throws EmailAlreadyRegisteredException {
		// Arrange
		ProcessModel model = new ProcessModel();
		model.setName(PROCESS_MODEL_NAME);

		ProcessDataStore dataStore = new ProcessDataStore();
		dataStore.setAccess(DataAccess.READ);
		dataStore.setElementId(DATA_STORE_ID);
		dataStore.setLabel(DATA_STORE_LABEL);
		model.setDataStores(Collections.singletonList(dataStore));

		User user = new User();
		user.setEmail(USER_EMAIL_1);
		user.setPassword(USER_PASSWORD);
		user.setRole(USER_ROLE);

		authenticationRepository.register(user);
		User fetchedUser = userRepositoryImpl.findByEmail(USER_EMAIL_1);
		Long userId = fetchedUser.getId();

		Long projectId = projectRepository.createProject(userId, PROJECT_NAME, PROJECT_VERSION).getId();

		processModelRepository.saveProcessModel(projectId, model);

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
	void testDeleteProjectWithProjectNotBelongingToUser() throws EmailAlreadyRegisteredException {
		// Arrange
		User user1 = new User();
		user1.setEmail(USER_EMAIL_1);
		user1.setPassword(USER_PASSWORD);
		user1.setRole(USER_ROLE);

		User user2 = new User();
		user2.setEmail(USER_EMAIL_2);
		user2.setPassword(USER_PASSWORD);
		user2.setRole(USER_ROLE);

		authenticationRepository.register(user1);
		authenticationRepository.register(user2);

		Long userId1 = userRepositoryImpl.findByEmail(USER_EMAIL_1).getId();
		Long userId2 = userRepositoryImpl.findByEmail(USER_EMAIL_2).getId();

		Long projectId = projectRepository.createProject(userId1, PROJECT_NAME, PROJECT_VERSION).getId();

		// Act & Assert
		assertThatThrownBy(() -> projectRepository.deleteProject(userId2, projectId))
				.isInstanceOf(NoResultException.class);
		assertThat(projectRepository.getProjects(userId1)).hasSize(1);
	}

	/**
	 * There is no quarkus feature to clean up the database
	 *
	 * @see <a href="https://stackoverflow.com/questions/71857904/quarkus-clean-h2-db-after-every-test"</a>
	 * @see <a href="https://github.com/quarkusio/quarkus/issues/14240"</a>
	 */
	@BeforeEach
	@Transactional
	void cleanupDatabase() {
		entityManager.createNativeQuery("DELETE FROM ProcessEventTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM CallActivityTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM ProcessConnectionTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM DataStoreConnectionTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM ProcessDataStoreTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM DataStoreTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM ProcessModelTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM ProjectTable").executeUpdate();
		entityManager.createNativeQuery("DELETE FROM UserTable").executeUpdate();
	}
}
