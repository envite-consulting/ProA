package de.envite.proa.repository;

import de.envite.proa.repository.processmodel.RelatedProcessModelDao;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import de.envite.proa.repository.tables.RelatedProcessModelTable;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RelatedProcessModelDaoTest {

    private static final String PROCESS_MODEL_NAME_1 = "process-model-name-1";
    private static final String PROCESS_MODEL_NAME_2 = "process-model-name-2";
    private static final String PROCESS_MODEL_NAME_3 = "process-model-name-3";
    private static final String BPMN_PROCESS_ID_1 = "bpmn-process-id-1";
    private static final String BPMN_PROCESS_ID_2 = "bpmn-process-id-2";
    private static final String BPMN_PROCESS_ID_3 = "bpmn-process-id-3";

    @Inject
    EntityManager em;

    @Inject
    RelatedProcessModelDao relatedProcessModelDao;

    private ProcessModelTable processModel1;
    private ProcessModelTable processModel2;
    private ProcessModelTable processModel3;
    private RelatedProcessModelTable manualRelation;
    private RelatedProcessModelTable autoRelation;

    @BeforeEach
    @Transactional
    void setUp() {
        ProjectTable project = new ProjectTable();
        em.persist(project);

        processModel1 = new ProcessModelTable();
        processModel1.setName(PROCESS_MODEL_NAME_1);
        processModel1.setBpmnProcessId(BPMN_PROCESS_ID_1);
        processModel1.setLevel(1);
        processModel1.setProject(project);
        em.persist(processModel1);

        processModel2 = new ProcessModelTable();
        processModel2.setName(PROCESS_MODEL_NAME_2);
        processModel2.setBpmnProcessId(BPMN_PROCESS_ID_2);
        processModel2.setLevel(2);
        processModel2.setProject(project);
        em.persist(processModel2);

        processModel3 = new ProcessModelTable();
        processModel3.setName(PROCESS_MODEL_NAME_3);
        processModel3.setBpmnProcessId(BPMN_PROCESS_ID_3);
        processModel3.setLevel(3);
        processModel3.setProject(project);
        em.persist(processModel3);

        autoRelation = new RelatedProcessModelTable();
        autoRelation.setRelatedProcessModelId(processModel2.getId());
        autoRelation.setLevel(processModel2.getLevel());
        autoRelation.setManuallyAdded(false);
        autoRelation.setProcessModel(processModel1);

        manualRelation = new RelatedProcessModelTable();
        manualRelation.setRelatedProcessModelId(processModel3.getId());
        manualRelation.setLevel(processModel3.getLevel());
        manualRelation.setManuallyAdded(true);
        manualRelation.setProcessModel(processModel1);
    }

    @AfterEach
    @Transactional
    void cleanup() {
        em.createQuery("DELETE FROM RelatedProcessModelTable").executeUpdate();
        em.createQuery("DELETE FROM ProcessModelTable").executeUpdate();
        em.createQuery("DELETE FROM ProjectTable").executeUpdate();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    @Transactional
    void testMergeRelatedProcessModel() {
        relatedProcessModelDao.merge(autoRelation);
        flushAndClear();

        List<RelatedProcessModelTable> retrievedRelatedProcessModels = relatedProcessModelDao.getRelatedProcessModels(processModel1);
        assertFalse(retrievedRelatedProcessModels.isEmpty());

        RelatedProcessModelTable persistedRelatedProcessModel = retrievedRelatedProcessModels.get(0);
        assertEquals(processModel2.getId(), persistedRelatedProcessModel.getRelatedProcessModelId());
        assertEquals(processModel2.getLevel(), persistedRelatedProcessModel.getLevel());
        assertFalse(persistedRelatedProcessModel.isManuallyAdded());
    }

    @Test
    @Transactional
    void testGetRelatedProcessModels() {
        relatedProcessModelDao.merge(autoRelation);
        relatedProcessModelDao.merge(manualRelation);
        flushAndClear();

        List<RelatedProcessModelTable> relations = relatedProcessModelDao.getRelatedProcessModels(processModel1);
        assertEquals(2, relations.size());
    }

    @Test
    @Transactional
    void testGetManuallyAddedRelations() {
        relatedProcessModelDao.merge(autoRelation);
        relatedProcessModelDao.merge(manualRelation);
        flushAndClear();

        List<RelatedProcessModelTable> manuallyAddedRelatedProcessModels = relatedProcessModelDao.getManuallyAddedRelations(processModel1);
        assertEquals(1, manuallyAddedRelatedProcessModels.size());
        assertTrue(manuallyAddedRelatedProcessModels.get(0).isManuallyAdded());
    }

    @Test
    @Transactional
    void testExistsManuallyAddedRelation() {
        boolean existsBeforeMerge = relatedProcessModelDao.existsManuallyAddedRelation(processModel1, processModel3);
        assertFalse(existsBeforeMerge);

        relatedProcessModelDao.merge(manualRelation);
        flushAndClear();

        boolean existsAfterMerge = relatedProcessModelDao.existsManuallyAddedRelation(processModel1, processModel3);
        assertTrue(existsAfterMerge);
    }

    @Test
    @Transactional
    void testDeleteByProcessModel() {
        relatedProcessModelDao.merge(manualRelation);
        flushAndClear();

        relatedProcessModelDao.deleteByProcessModel(processModel1);
        flushAndClear();

        List<RelatedProcessModelTable> relatedProcessModels = relatedProcessModelDao.getRelatedProcessModels(processModel1);
        assertTrue(relatedProcessModels.isEmpty());
    }

    @Test
    @Transactional
    void testDeleteByRelatedProcessModelId() {
        relatedProcessModelDao.merge(manualRelation);
        flushAndClear();

        relatedProcessModelDao.deleteByRelatedProcessModelId(processModel3.getId());
        flushAndClear();

        List<RelatedProcessModelTable> relations = relatedProcessModelDao.getRelatedProcessModels(processModel1);
        assertTrue(relations.isEmpty());
    }

    @Test
    @Transactional
    void testDeleteAutoGeneratedByProcessModel() {
        relatedProcessModelDao.merge(autoRelation);
        relatedProcessModelDao.merge(manualRelation);
        flushAndClear();

        relatedProcessModelDao.deleteAutoGeneratedByProcessModel(processModel1);
        flushAndClear();

        List<RelatedProcessModelTable> relatedProcessModels = relatedProcessModelDao.getRelatedProcessModels(processModel1);
        assertEquals(1, relatedProcessModels.size());
        assertTrue(relatedProcessModels.get(0).isManuallyAdded());
    }
}
