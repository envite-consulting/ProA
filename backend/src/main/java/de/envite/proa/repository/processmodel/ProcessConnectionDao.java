package de.envite.proa.repository.processmodel;

import de.envite.proa.repository.tables.ProcessConnectionTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@RequestScoped
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
	public List<ProcessConnectionTable> getProcessConnections(ProjectVersionTable projectVersionTable) {
		return em//
				.createQuery("SELECT pc FROM ProcessConnectionTable pc WHERE pc.project = :project",
						ProcessConnectionTable.class)//
				.setParameter("project", projectVersionTable).getResultList();
	}

	@Transactional
	public List<ProcessConnectionTable> getProcessConnections(ProjectVersionTable projectVersionTable,
			ProcessModelTable processModel) {
		return em //
				.createQuery("SELECT pc " + //
								"FROM ProcessConnectionTable pc " + //
								"WHERE pc.project = :project " + //
								"AND (pc.callingProcess = :processModel OR pc.calledProcess = :processModel)", //
						ProcessConnectionTable.class) //
				.setParameter("project", projectVersionTable) //
				.setParameter("processModel", processModel).getResultList();
	}

	@Transactional
	public void deleteForProcessModel(Long id) {
		ProcessModelTable processModel = em.find(ProcessModelTable.class, id);
		em.createQuery(
						"DELETE FROM ProcessConnectionTable pc WHERE pc.callingProcess = :processModel OR pc.calledProcess = :processModel")
				.setParameter("processModel", processModel)//
				.executeUpdate();
	}

	@Transactional
	public void addConnection(ProcessConnectionTable processConnection) {
		em.persist(processConnection);
	}

	@Transactional
	public void deleteConnection(Long connectionId) {
		ProcessConnectionTable processConnection = em.find(ProcessConnectionTable.class, connectionId);
		em.remove(processConnection);
	}
}