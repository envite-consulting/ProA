package de.envite.proa.repository;

import java.util.List;

import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.ProjectTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CallActivityDao {

	private EntityManager em;

	@Inject
	public CallActivityDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public void persist(CallActivityTable table) {
		em.persist(table);
	}

	@Transactional
	public List<CallActivityTable> getCallActivitiesForName(String name, ProjectTable projectTable) {
		return em //
				.createQuery("SELECT c FROM CallActivityTable c WHERE c.label = :label AND c.project = :project",
						CallActivityTable.class)
				.setParameter("label", name)//
				.setParameter("project", projectTable)//
				.getResultList();
	}
}