package de.envite.proa.repository;

import java.util.List;

import de.envite.proa.repository.tables.ProcessConnectionTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProcessConnectionDao {

	private EntityManager em;

	@Inject
	public ProcessConnectionDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public void persist(ProcessConnectionTable table) {
		em.persist(table);
	}

	@Transactional
	public List<ProcessConnectionTable> getProcessConnections(ProjectTable projectTable) {
		return em//
				.createQuery("SELECT pc FROM ProcessConnectionTable pc WHERE pc.project = :project", ProcessConnectionTable.class)//
				.setParameter("project", projectTable)
				.getResultList();
	}

	@Transactional
	public void deleteForProcessModel(Long id) {

		ProcessModelTable processModel = em.find(ProcessModelTable.class, id);
		em.createQuery(
				"DELETE FROM ProcessConnectionTable pc WHERE pc.callingProcess = :processModel OR pc.calledProcess = :processModel")
				.setParameter("processModel", processModel)//
				.executeUpdate();
	}
}