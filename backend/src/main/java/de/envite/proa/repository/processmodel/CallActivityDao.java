package de.envite.proa.repository.processmodel;

import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectVersionTable;
import de.envite.proa.util.SearchLabelBuilder;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@RequestScoped
public class CallActivityDao {

    @ConfigProperty(name = "quarkus.datasource.db-kind")
    String dbKind;

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
	public List<CallActivityTable> getCallActivitiesForName(String label, ProjectVersionTable projectVersionTable) {

		String searchLabel = SearchLabelBuilder.buildSearchLabel(label);

		if ("postgresql".equals(dbKind)) {
			return em.createQuery(
							 "SELECT c FROM CallActivityTable c " +
								"WHERE c.project = :project " +
								"AND ( c.searchLabel = :searchLabel " +
								"OR function('levenshtein', c.searchLabel, :searchLabel) <= 4 )",
							CallActivityTable.class)
					.setParameter("searchLabel", searchLabel)//
					.setParameter("project", projectVersionTable)//
					.getResultList();
		} else {
			return em.createQuery(
							 "SELECT c FROM CallActivityTable c " +
								"WHERE c.project = :project " +
								"AND c.searchLabel = :searchLabel ",
							CallActivityTable.class)
					.setParameter("searchLabel", searchLabel)//
					.setParameter("project", projectVersionTable)//
					.getResultList();
		}
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