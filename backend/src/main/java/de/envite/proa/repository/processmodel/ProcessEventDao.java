package de.envite.proa.repository.processmodel;

import java.util.Arrays;
import java.util.List;

import de.envite.proa.entities.process.EventType;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestScoped
public class ProcessEventDao {

	private EntityManager em;

	@Inject
	public ProcessEventDao(EntityManager em) {
		this.em = em;
	}

    @Transactional
    public List<ProcessEventTable> getEventsForLabelAndType(String label, EventType eventType,
                                                            ProjectTable projectTable) {

        String searchLabel = buildSearchLabel(label);

        return em.createQuery(
                "SELECT e FROM ProcessEventTable e " +
                    "WHERE e.eventType = :eventType " +
                    "AND e.project = :project " +
                    "AND ( e.searchLabel = :searchLabel " +
                    "OR function('DIFFERENCE', e.searchLabel, :searchLabel) >= 3 )",
                ProcessEventTable.class)
                .setParameter("searchLabel", searchLabel)
                .setParameter("eventType", eventType)
                .setParameter("project", projectTable)
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

    public static String buildSearchLabel(String label) {
        if (label == null) return null;
        String cleanedLabel = label
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s]", " ")
                .trim()
                .replaceAll("\\s+", " ");
        String[] words = cleanedLabel.split("\\s+");
        Arrays.sort(words);
        return String.join("", words);
    }
}