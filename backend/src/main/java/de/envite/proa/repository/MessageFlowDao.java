package de.envite.proa.repository;

import de.envite.proa.repository.tables.MessageFlowTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class MessageFlowDao {

	private final EntityManager em;

	@Inject
	public MessageFlowDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public void persist(MessageFlowTable table) {
		em.persist(table);
	}

	@Transactional
	public void merge(MessageFlowTable table) {
		em.merge(table);
	}

	@Transactional
	public List<MessageFlowTable> getMessageFlows(ProjectTable projectTable) {
		return em.createQuery( //
				"SELECT mf " + //
						"FROM MessageFlowTable mf " + //
						"WHERE mf.project = :project", //
				MessageFlowTable.class //
		) //
				.setParameter("project", projectTable) //
				.getResultList(); //
	}

	@Transactional
	public List<MessageFlowTable> getMessageFlows(ProjectTable projectTable, ProcessModelTable processModel) {
		return em.createQuery( //
				"SELECT mf " + //
						"FROM MessageFlowTable mf " + //
						"WHERE (mf.callingProcess = :processModel " + //
						"OR mf.calledProcess = :processModel) " + //
						"AND mf.project = :project", //
				MessageFlowTable.class //
		) //
				.setParameter("processModel", processModel) //
				.setParameter("project", projectTable) //
				.getResultList(); //
	}

	@Transactional
	public void deleteForProcessModel(Long id) {
		ProcessModelTable processModel = new ProcessModelTable();
		processModel.setId(id);
		em.createQuery(
						"DELETE FROM MessageFlowTable mf WHERE mf.callingProcess = :processModel OR mf.calledProcess = :processModel")
				.setParameter("processModel", processModel)//
				.executeUpdate();
	}
}
