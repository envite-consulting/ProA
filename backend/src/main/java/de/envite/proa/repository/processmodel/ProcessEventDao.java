package de.envite.proa.repository.processmodel;

import de.envite.proa.entities.process.EventType;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@RequestScoped
public class ProcessEventDao {

	private EntityManager em;

	@Inject
	public ProcessEventDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public List<ProcessEventTable> getEventsForLabelAndType(String label, EventType eventType,
			ProjectVersionTable projectVersionTable) {

		return em //
				.createQuery(
						"SELECT e FROM ProcessEventTable e WHERE e.label = :label AND e.eventType=:eventType AND e.project=:project",
						ProcessEventTable.class)
				.setParameter("label", label)//
				.setParameter("eventType", eventType)//
				.setParameter("project", projectVersionTable)//
				.getResultList();
	}

	@Transactional
	public ProcessEventTable findForProcessModelAndEventType(ProcessModelTable processModel, EventType eventType) {
		return em.createQuery(
						"SELECT e FROM ProcessEventTable e WHERE e.processModel = :processModel AND e.eventType = :eventType",
						ProcessEventTable.class) //
				.setParameter("processModel", processModel) //
				.setParameter("eventType", eventType) //
				.getResultList() //
				.stream() //
				.findFirst() //
				.orElse(null);
	}
}