package de.envite.proa.repository;

import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.RelatedProcessModelTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class RelatedProcessModelDao {
    private final EntityManager em;

    @Inject
    public RelatedProcessModelDao(EntityManager em) {
        this.em = em;
    }

    @Transactional
    public void merge(RelatedProcessModelTable table) {
        em.merge(table);
    }

    @Transactional
    public List<RelatedProcessModelTable> getRelatedProcessModels(ProcessModelTable processModel) {
        return em
                .createQuery("SELECT rpm FROM RelatedProcessModelTable rpm WHERE rpm.processModel = :processModel", RelatedProcessModelTable.class)
                .setParameter("processModel", processModel)
                .getResultList();
    }

    @Transactional
    public void deleteByProcessModel(ProcessModelTable processModel) {
        em.createQuery("DELETE FROM RelatedProcessModelTable rpm WHERE rpm.processModel = :processModel")
                .setParameter("processModel", processModel)
                .executeUpdate();
    }

    @Transactional
    public void deleteByRelatedProcessModelId(Long relatedProcessModelId) {
        em.createQuery("DELETE FROM RelatedProcessModelTable rpm WHERE rpm.relatedProcessModelId = :relatedProcessModelId")
                .setParameter("relatedProcessModelId", relatedProcessModelId)
                .executeUpdate();
    }
}
