package de.envite.proa.repository.processmodel;

import java.util.List;

import de.envite.proa.entities.process.EventType;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import de.envite.proa.util.SearchLabelBuilder;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@RequestScoped
public class ProcessEventDao {

    @ConfigProperty(name = "quarkus.datasource.db-kind")
    String dbKind;

	private EntityManager em;

	@Inject
	public ProcessEventDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public List<ProcessEventTable> getEventsForLabelAndType(String label, EventType eventType,
			ProjectVersionTable projectVersionTable) {

		String searchLabel = SearchLabelBuilder.buildSearchLabel(label);

		if ("postgresql".equals(dbKind)) {
			return em.createQuery(
							 "SELECT e FROM ProcessEventTable e " +
								"WHERE e.eventType = :eventType " +
								"AND e.project = :project " +
								"AND ( e.searchLabel = :searchLabel " +
								"OR function('levenshtein', e.searchLabel, :searchLabel) <= 4 )",
							ProcessEventTable.class)
					.setParameter("searchLabel", searchLabel)
					.setParameter("eventType", eventType)
					.setParameter("project", projectVersionTable)
					.getResultList();
		} else {
			return em.createQuery(
							 "SELECT e FROM ProcessEventTable e " +
								"WHERE e.eventType = :eventType " +
								"AND e.project = :project " +
								"AND e.searchLabel = :searchLabel ",
							ProcessEventTable.class)
					.setParameter("searchLabel", searchLabel)
					.setParameter("eventType", eventType)
					.setParameter("project", projectVersionTable)
					.getResultList();
		}
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