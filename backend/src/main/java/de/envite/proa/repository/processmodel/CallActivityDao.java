package de.envite.proa.repository.processmodel;

import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@RequestScoped
public class CallActivityDao {

	private final EntityManager em;

	@Inject
	public CallActivityDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public void persist(CallActivityTable table) {
		em.persist(table);
	}

	@Transactional
	public List<CallActivityTable> getCallActivitiesForName(String name, ProjectVersionTable project) {
		return em //
				.createQuery("SELECT c FROM CallActivityTable c WHERE c.label = :label AND c.project = :project",
						CallActivityTable.class)
				.setParameter("label", name)//
				.setParameter("project", project)//
				.getResultList();
	}

	@Transactional
	public CallActivityTable findForProcessModel(ProcessModelTable processModel) {
		return em
				.createQuery("SELECT c FROM CallActivityTable c WHERE c.processModel = :processModel",
						CallActivityTable.class)
				.setParameter("processModel", processModel)//
				.getResultList() //
				.stream() //
				.findFirst() //
				.orElse(null);
	}
}