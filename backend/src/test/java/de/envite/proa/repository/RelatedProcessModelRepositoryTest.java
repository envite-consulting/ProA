package de.envite.proa.repository;

import de.envite.proa.entities.process.EventType;
import de.envite.proa.entities.process.ProcessType;
import de.envite.proa.entities.process.RelatedProcessModel;
import de.envite.proa.repository.processmodel.ProcessModelDao;
import de.envite.proa.repository.processmodel.RelatedProcessModelDao;
import de.envite.proa.repository.processmodel.RelatedProcessModelRepositoryImpl;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.RelatedProcessModelTable;
import jakarta.persistence.NoResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RelatedProcessModelRepositoryTest {

    private static final Long PROJECT_ID = 1L;
    private static final Long PROCESS_MODEL_ID_1 = 1L;
    private static final Long PROCESS_MODEL_ID_2 = 2L;
    private static final Long PROCESS_MODEL_ID_3 = 3L;
    private static final String PROCESS_MODEL_NAME_1 = "Recruiting_Level1";
    private static final String PROCESS_MODEL_NAME_2 = "Recruiting_Level2";
    private static final String PROCESS_MODEL_NAME_3 = "Recruiting_Level3";
    private static final String BPMN_PROCESS_ID_1 = "Collaboration_1b2p3ms";
    private static final String BPMN_PROCESS_ID_2 = "Collaboration_1b2p3mo";
    private static final String BPMN_PROCESS_ID_3 = "Collaboration_1b2p3mt";
    private static final String START_EVENT_LABEL_1 = "Vacancy arises";
    private static final String END_EVENT_LABEL_1 = "Vacancy filled";
    private static final String START_EVENT_LABEL_2 = "Vacancy reported";
    private static final String END_EVENT_LABEL_2 = "Job advertised";

    @InjectMocks
    private RelatedProcessModelRepositoryImpl repository;
    @Mock
    private ProcessModelDao processModelDao;
    @Mock
    private RelatedProcessModelDao relatedProcessModelDao;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalculateAndSaveRelatedProcessModels_modelsWithSameStartAndEndEventLabels() {
        // Arrange
        ProjectTable project = createProject();

        ProcessModelTable modelLevel1 = createProcessModel(
                PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, BPMN_PROCESS_ID_1, new byte[8]);
        ProcessModelTable modelLevel2 = createProcessModel(
                PROCESS_MODEL_ID_2, PROCESS_MODEL_NAME_2, BPMN_PROCESS_ID_2, new byte[18]);

        ProcessEventTable startEventModelLevel1 = createProcessEvent(START_EVENT_LABEL_1, EventType.START);
        ProcessEventTable endEventModelLevel1 = createProcessEvent(END_EVENT_LABEL_1, EventType.END);
        ProcessEventTable startEventModelLevel2 = createProcessEvent(START_EVENT_LABEL_1, EventType.START);
        ProcessEventTable endEventModelLevel2 = createProcessEvent(END_EVENT_LABEL_1, EventType.END);

        modelLevel1.setProject(project);
        modelLevel1.setEvents(Set.of(startEventModelLevel1, endEventModelLevel1));
        modelLevel2.setProject(project);
        modelLevel2.setEvents(Set.of(startEventModelLevel2, endEventModelLevel2));

        when(processModelDao.getProcessModelsWithEvents(project))
                .thenReturn(List.of(modelLevel1, modelLevel2));
        when(processModelDao.getBpmnXml(PROCESS_MODEL_ID_1)).thenReturn(new byte[8]);
        when(processModelDao.getBpmnXml(PROCESS_MODEL_ID_2)).thenReturn(new byte[18]);

        // Act
        repository.calculateAndSaveRelatedProcessModels(project);

        // Assert
        ArgumentCaptor<RelatedProcessModelTable> captor = ArgumentCaptor.forClass(RelatedProcessModelTable.class);
        verify(relatedProcessModelDao, atLeastOnce()).merge(captor.capture());
        verify(processModelDao, atLeastOnce()).merge(any(ProcessModelTable.class));

        List<RelatedProcessModelTable> capturedRelatedModels = captor.getAllValues();

        assertEquals(1, modelLevel1.getLevel());
        assertEquals(2, modelLevel2.getLevel());

        List<RelatedProcessModelTable> relatedModelsOfLevel1Process = capturedRelatedModels.stream()
                .filter(rpm -> rpm.getProcessModel().equals(modelLevel1))
                .toList();
        assertEquals(1, relatedModelsOfLevel1Process.size());

        List<RelatedProcessModelTable> relatedModelsOfLevel2Process = capturedRelatedModels.stream()
                .filter(rpm -> rpm.getProcessModel().equals(modelLevel2))
                .toList();
        assertEquals(1, relatedModelsOfLevel2Process.size());

        Optional<RelatedProcessModelTable> relatedModelLevel1Process =
                relatedModelsOfLevel1Process.stream().findFirst();
        assertTrue(relatedModelLevel1Process.isPresent());
        assertEquals(PROCESS_MODEL_ID_2, relatedModelLevel1Process.get().getRelatedProcessModelId());
        assertEquals(PROCESS_MODEL_NAME_2, relatedModelLevel1Process.get().getProcessName());
        assertEquals(2, relatedModelLevel1Process.get().getLevel());

        Optional<RelatedProcessModelTable> relatedModelLevel2Process =
                relatedModelsOfLevel2Process.stream().findFirst();
        assertTrue(relatedModelLevel2Process.isPresent());
        assertEquals(PROCESS_MODEL_ID_1, relatedModelLevel2Process.get().getRelatedProcessModelId());
        assertEquals(PROCESS_MODEL_NAME_1, relatedModelLevel2Process.get().getProcessName());
        assertEquals(1, relatedModelLevel2Process.get().getLevel());
    }

    @Test
    void testCalculateAndSaveRelatedProcessModels_modelsWithoutStartAndEndEvents() {
        // Arrange
        ProjectTable project = createProject();

        ProcessModelTable modelLevel1 = createProcessModel(
                PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, BPMN_PROCESS_ID_1, new byte[8]);
        ProcessModelTable modelLevel2 = createProcessModel(
                PROCESS_MODEL_ID_2, PROCESS_MODEL_NAME_2, BPMN_PROCESS_ID_2, new byte[18]);

        modelLevel1.setProject(project);
        modelLevel2.setProject(project);

        when(processModelDao.getProcessModelsWithEvents(project))
                .thenReturn(List.of(modelLevel1, modelLevel2));

        // Act
        repository.calculateAndSaveRelatedProcessModels(project);

        // Assert
        verify(processModelDao, times(0)).merge(any());
        verify(relatedProcessModelDao, times(0)).merge(any());

        List<RelatedProcessModelTable> relatedModelsOfLevel1Process = modelLevel1.getRelatedProcessModels();
        List<RelatedProcessModelTable> relatedModelsOfLevel2Process = modelLevel2.getRelatedProcessModels();

        assertEquals(0, relatedModelsOfLevel1Process.size());
        assertEquals(0, relatedModelsOfLevel2Process.size());
    }

    @Test
    void testAddRelatedProcessModel_validCase() {
        // Arrange
        ProjectTable project = createProject();

        ProcessModelTable modelLevel1 = createProcessModel(
                PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, BPMN_PROCESS_ID_1, new byte[8]);
        ProcessModelTable modelLevel2 = createProcessModel(
                PROCESS_MODEL_ID_2, PROCESS_MODEL_NAME_2, BPMN_PROCESS_ID_2, new byte[18]);
        ProcessModelTable modelLevel3 = createProcessModel(
                PROCESS_MODEL_ID_3, PROCESS_MODEL_NAME_3, BPMN_PROCESS_ID_3, new byte[36]);

        ProcessEventTable startEventModelLevel1 = createProcessEvent(START_EVENT_LABEL_1, EventType.START);
        ProcessEventTable endEventModelLevel1 = createProcessEvent(END_EVENT_LABEL_1, EventType.END);
        ProcessEventTable startEventModelLevel2 = createProcessEvent(START_EVENT_LABEL_1, EventType.START);
        ProcessEventTable endEventModelLevel2 = createProcessEvent(END_EVENT_LABEL_1, EventType.END);
        ProcessEventTable startEventModelLevel3 = createProcessEvent(START_EVENT_LABEL_2, EventType.START);
        ProcessEventTable endEventModelLevel3 = createProcessEvent(END_EVENT_LABEL_2, EventType.END);

        modelLevel1.setProject(project);
        modelLevel1.setEvents(Set.of(startEventModelLevel1, endEventModelLevel1));
        modelLevel2.setProject(project);
        modelLevel2.setEvents(Set.of(startEventModelLevel2, endEventModelLevel2));
        modelLevel3.setProject(project);
        modelLevel3.setEvents(Set.of(startEventModelLevel3, endEventModelLevel3));

        when(processModelDao.find(PROCESS_MODEL_ID_3)).thenReturn(modelLevel3);
        when(processModelDao.getProcessModelsByIds(eq(project), any()))
                .thenReturn(List.of(modelLevel1, modelLevel2));
        when(processModelDao.getBpmnXml(PROCESS_MODEL_ID_1)).thenReturn(new byte[8]);
        when(processModelDao.getBpmnXml(PROCESS_MODEL_ID_2)).thenReturn(new byte[18]);
        when(processModelDao.getBpmnXml(PROCESS_MODEL_ID_3)).thenReturn(new byte[36]);

        // Act
        repository.addRelatedProcessModel(
                PROJECT_ID, PROCESS_MODEL_ID_3, List.of(PROCESS_MODEL_ID_1, PROCESS_MODEL_ID_2));

        // Assert
        ArgumentCaptor<RelatedProcessModelTable> captor = ArgumentCaptor.forClass(RelatedProcessModelTable.class);
        verify(relatedProcessModelDao, atLeastOnce()).merge(captor.capture());
        verify(processModelDao, atLeastOnce()).merge(any(ProcessModelTable.class));

        List<RelatedProcessModelTable> capturedRelatedModels = captor.getAllValues();

        assertEquals(1, modelLevel1.getLevel());
        assertEquals(2, modelLevel2.getLevel());
        assertEquals(3, modelLevel3.getLevel());

        List<RelatedProcessModelTable> relatedModelsOfLevel1Process = capturedRelatedModels.stream()
                .filter(rpm -> rpm.getProcessModel().equals(modelLevel1))
                .toList();
        assertThat(relatedModelsOfLevel1Process)
                .hasSize(2)
                .extracting("relatedProcessModelId", "processName", "level")
                .contains(
                        tuple(PROCESS_MODEL_ID_2, PROCESS_MODEL_NAME_2, 2),
                        tuple(PROCESS_MODEL_ID_3, PROCESS_MODEL_NAME_3, 3)
                );

        List<RelatedProcessModelTable> relatedModelsOfLevel2Process = capturedRelatedModels.stream()
                .filter(rpm -> rpm.getProcessModel().equals(modelLevel2))
                .toList();
        assertThat(relatedModelsOfLevel2Process)
                .hasSize(2)
                .extracting("relatedProcessModelId", "processName", "level")
                .contains(
                        tuple(PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, 1),
                        tuple(PROCESS_MODEL_ID_3, PROCESS_MODEL_NAME_3, 3)
                );

        List<RelatedProcessModelTable> relatedModelsOfLevel3Process = capturedRelatedModels.stream()
                .filter(rpm -> rpm.getProcessModel().equals(modelLevel3))
                .toList();
        assertThat(relatedModelsOfLevel3Process)
                .hasSize(2)
                .extracting("relatedProcessModelId", "processName", "level")
                .contains(
                        tuple(PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, 1),
                        tuple(PROCESS_MODEL_ID_2, PROCESS_MODEL_NAME_2, 2)
                );
    }

    @Test
    void testAddRelatedProcessModel_processModelNotFound() {
        // Arrange
        ProjectTable project = createProject();

        ProcessModelTable modelLevel1 = createProcessModel(
                PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, BPMN_PROCESS_ID_1, new byte[8]);
        ProcessModelTable modelLevel2 = createProcessModel(
                PROCESS_MODEL_ID_2, PROCESS_MODEL_NAME_2, BPMN_PROCESS_ID_2, new byte[18]);

        ProcessEventTable startEventModelLevel1 = createProcessEvent(START_EVENT_LABEL_1, EventType.START);
        ProcessEventTable endEventModelLevel1 = createProcessEvent(END_EVENT_LABEL_1, EventType.END);
        ProcessEventTable startEventModelLevel2 = createProcessEvent(START_EVENT_LABEL_1, EventType.START);
        ProcessEventTable endEventModelLevel2 = createProcessEvent(END_EVENT_LABEL_1, EventType.END);

        modelLevel1.setProject(project);
        modelLevel1.setEvents(Set.of(startEventModelLevel1, endEventModelLevel1));
        modelLevel2.setProject(project);
        modelLevel2.setEvents(Set.of(startEventModelLevel2, endEventModelLevel2));

        when(processModelDao.find(PROCESS_MODEL_ID_3)).thenReturn(null);
        when(processModelDao.getProcessModelsByIds(eq(project), any()))
                .thenReturn(List.of(modelLevel1, modelLevel2));

        // Act & Assert
        List<Long> relatedProcessModels = List.of(PROCESS_MODEL_ID_1, PROCESS_MODEL_ID_2);
        assertThrows(NoResultException.class, () ->
                repository.addRelatedProcessModel(PROJECT_ID, PROCESS_MODEL_ID_1, relatedProcessModels)
        );
    }

    @Test
    void testAddRelatedProcessModel_relatedProcessModelNotFound() {
        // Arrange
        ProjectTable project = createProject();

        ProcessModelTable modelLevel1 = createProcessModel(
                PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, BPMN_PROCESS_ID_1, new byte[8]);

        ProcessEventTable startEventModelLevel1 = createProcessEvent(START_EVENT_LABEL_1, EventType.START);
        ProcessEventTable endEventModelLevel1 = createProcessEvent(END_EVENT_LABEL_1, EventType.END);

        modelLevel1.setProject(project);
        modelLevel1.setEvents(Set.of(startEventModelLevel1, endEventModelLevel1));

        when(processModelDao.find(PROCESS_MODEL_ID_1)).thenReturn(modelLevel1);
        when(processModelDao.getProcessModelsByIds(eq(project), any()))
                .thenReturn(List.of());

        // Act & Assert
        List<Long> relatedProcessModelIds = List.of(PROCESS_MODEL_ID_2);
        assertThrows(NoResultException.class, () ->
                repository.addRelatedProcessModel(PROJECT_ID, PROCESS_MODEL_ID_1, relatedProcessModelIds)
        );
    }

    @Test
    void testAddRelatedProcessModel_selfRelationNotAllowed() {
        // Arrange
        ProjectTable project = createProject();

        ProcessModelTable modelLevel1 = createProcessModel(
                PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, BPMN_PROCESS_ID_1, new byte[8]);
        ProcessModelTable modelLevel2 = createProcessModel(
                PROCESS_MODEL_ID_2, PROCESS_MODEL_NAME_2, BPMN_PROCESS_ID_2, new byte[18]);

        modelLevel1.setProject(project);
        modelLevel2.setProject(project);

        when(processModelDao.find(PROCESS_MODEL_ID_1)).thenReturn(modelLevel1);
        when(processModelDao.getProcessModelsByIds(eq(project), any()))
                .thenReturn(List.of(modelLevel1, modelLevel2));

        // Act & Assert
        List<Long> relatedProcessModelIds = List.of(PROCESS_MODEL_ID_1, PROCESS_MODEL_ID_2);
        assertThrows(IllegalArgumentException.class, () ->
                repository.addRelatedProcessModel(PROJECT_ID, PROCESS_MODEL_ID_1, relatedProcessModelIds)
        );
    }

    @Test
    void testAddRelatedProcessModel_processModelWithProcessTypeParticipantNotAllowed() {
        // Arrange
        ProjectTable project = createProject();

        ProcessModelTable modelLevel1 = createProcessModel(
                PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, BPMN_PROCESS_ID_1, new byte[8]);
        ProcessModelTable modelLevel2 = createProcessModel(
                PROCESS_MODEL_ID_2, PROCESS_MODEL_NAME_2, BPMN_PROCESS_ID_2, new byte[18]);

        ProcessEventTable startEventModelLevel1 = createProcessEvent(START_EVENT_LABEL_1, EventType.START);
        ProcessEventTable endEventModelLevel1 = createProcessEvent(END_EVENT_LABEL_1, EventType.END);
        ProcessEventTable startEventModelLevel2 = createProcessEvent(START_EVENT_LABEL_1, EventType.START);
        ProcessEventTable endEventModelLevel2 = createProcessEvent(END_EVENT_LABEL_1, EventType.END);

        modelLevel1.setProcessType(ProcessType.PARTICIPANT);
        modelLevel1.setProject(project);
        modelLevel1.setEvents(Set.of(startEventModelLevel1, endEventModelLevel1));
        modelLevel2.setProject(project);
        modelLevel2.setEvents(Set.of(startEventModelLevel2, endEventModelLevel2));

        when(processModelDao.find(PROCESS_MODEL_ID_1)).thenReturn(modelLevel1);
        when(processModelDao.getProcessModelsByIds(eq(project), any()))
                .thenReturn(List.of(modelLevel2));

        // Act & Assert
        List<Long> relatedProcessModelIds = List.of(PROCESS_MODEL_ID_2);
        assertThrows(IllegalArgumentException.class, () ->
                repository.addRelatedProcessModel(PROJECT_ID, PROCESS_MODEL_ID_1, relatedProcessModelIds)
        );
    }

    @Test
    void testAddRelatedProcessModel_relatedProcessModelWithProcessTypeParticipantNotAllowed() {
        // Arrange
        ProjectTable project = createProject();

        ProcessModelTable modelLevel1 = createProcessModel(
                PROCESS_MODEL_ID_1, PROCESS_MODEL_NAME_1, BPMN_PROCESS_ID_1, new byte[8]);
        ProcessModelTable modelLevel2 = createProcessModel(
                PROCESS_MODEL_ID_2, PROCESS_MODEL_NAME_2, BPMN_PROCESS_ID_2, new byte[18]);

        ProcessEventTable startEventModelLevel1 = createProcessEvent(START_EVENT_LABEL_1, EventType.START);
        ProcessEventTable endEventModelLevel1 = createProcessEvent(END_EVENT_LABEL_1, EventType.END);
        ProcessEventTable startEventModelLevel2 = createProcessEvent(START_EVENT_LABEL_1, EventType.START);
        ProcessEventTable endEventModelLevel2 = createProcessEvent(END_EVENT_LABEL_1, EventType.END);

        modelLevel1.setProject(project);
        modelLevel1.setEvents(Set.of(startEventModelLevel1, endEventModelLevel1));
        modelLevel2.setProcessType(ProcessType.PARTICIPANT);
        modelLevel2.setProject(project);
        modelLevel2.setEvents(Set.of(startEventModelLevel2, endEventModelLevel2));

        when(processModelDao.find(PROCESS_MODEL_ID_1)).thenReturn(modelLevel1);
        when(processModelDao.getProcessModelsByIds(eq(project), any()))
                .thenReturn(List.of(modelLevel2));

        // Act & Assert
        List<Long> relatedProcessModelIds = List.of(PROCESS_MODEL_ID_2);
        assertThrows(IllegalArgumentException.class, () ->
                repository.addRelatedProcessModel(PROJECT_ID, PROCESS_MODEL_ID_1, relatedProcessModelIds)
        );
    }

    @Test
    void testMapToRelatedProcessModel() {
        // Arrange
        RelatedProcessModelTable relatedProcessModelTable = new RelatedProcessModelTable();
        relatedProcessModelTable.setRelatedProcessModelId(1L);
        relatedProcessModelTable.setProcessName("processName");
        relatedProcessModelTable.setLevel(null);
        relatedProcessModelTable.setManuallyAdded(false);

        // Act
        RelatedProcessModelRepositoryImpl repository = new RelatedProcessModelRepositoryImpl(null, null);
        RelatedProcessModel result = repository.mapToRelatedProcessModel(relatedProcessModelTable);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getRelatedProcessModelId());
        assertEquals("processName", result.getProcessName());
        assertNull(result.getLevel());
        assertFalse(result.isManuallyAdded());
    }

    private ProjectTable createProject() {
        ProjectTable project = new ProjectTable();
        project.setId(PROJECT_ID);

        return project;
    }

    private ProcessModelTable createProcessModel(Long id, String name, String bpmnProcessId, byte[] bpmnXml) {
        ProcessModelTable processModel = new ProcessModelTable();
        processModel.setId(id);
        processModel.setName(name);
        processModel.setBpmnProcessId(bpmnProcessId);
        processModel.setBpmnXml(bpmnXml);

        return processModel;
    }

    private ProcessEventTable createProcessEvent(String label, EventType eventType) {
        ProcessEventTable processEvent = new ProcessEventTable();
        processEvent.setLabel(label);
        processEvent.setEventType(eventType);

        return processEvent;
    }
}
