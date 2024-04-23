package de.envite.proa.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProcessModelDao {

	private EntityManager em;

	@Inject
	public ProcessModelDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public List<ProcessModelTable> getProcessModels(ProjectTable projectTable) {
		return em//
				.createQuery("SELECT pm FROM ProcessModelTable pm WHERE pm.project=:project", ProcessModelTable.class)//
				.setParameter("project", projectTable)//
				.getResultList();
	}

	@Transactional
	public List<ProcessModelTable> getProcessModelsForName(String name, ProjectTable projectTable) {
		return em //
				.createQuery("SELECT p FROM ProcessModelTable p WHERE p.name = :name AND p.project =:project",
						ProcessModelTable.class)
				.setParameter("name", name)//
				.setParameter("project", projectTable)//
				.getResultList();
	}

	@Transactional
	public void persist(ProcessModelTable table) {
		em.persist(table);
	}

	@Transactional
	public ProcessModelTable findWithChildren(Long id) {

		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);
		graph.addSubgraph("events");

		Map<String, Object> hints = new HashMap<String, Object>();
		hints.put("javax.persistence.loadgraph", graph);

		return em.find(ProcessModelTable.class, id, hints);
	}

	@Transactional
	public ProcessModelTable find(Long id) {

		return em.find(ProcessModelTable.class, id);
	}

	@Transactional
	public void delete(Long id) {
		ProcessModelTable table = em.find(ProcessModelTable.class, id);
		em.remove(table);
	}
}
