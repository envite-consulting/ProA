package de.envite.proa.repository;

import de.envite.proa.repository.tables.ProcessLevelTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class ProcessLevelDao {
    private final EntityManager em;

    @Inject
    public ProcessLevelDao(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void merge(ProcessLevelTable level) {
        em.merge(level);
    }

    @Transactional
    public List<ProcessLevelTable> getProcessLevels(ProcessModelTable processModel) {
        return em
                .createQuery("SELECT pl FROM ProcessLevelTable pl WHERE pl.processModel = :processModel", ProcessLevelTable.class)
                .setParameter("processModel", processModel)
                .getResultList();
    }

    @Transactional
    public void deleteByProcessModel(ProcessModelTable processModel) {
        em.createQuery("DELETE FROM ProcessLevelTable pl WHERE pl.processModel = :processModel")
                .setParameter("processModel", processModel)
                .executeUpdate();
    }

    @Transactional
    public void deleteByRelatedProcessModelId(Long relatedProcessModelId) {
        em.createQuery("DELETE FROM ProcessLevelTable pl WHERE pl.relatedProcessModelId = :relatedProcessModelId")
                .setParameter("relatedProcessModelId", relatedProcessModelId)
                .executeUpdate();
    }
}
