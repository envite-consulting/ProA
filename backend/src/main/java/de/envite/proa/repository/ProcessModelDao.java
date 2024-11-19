package de.envite.proa.repository;

import java.util.Arrays;
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

		List<ProcessModelTable> processModels = em//
				.createQuery("""
						SELECT pm FROM ProcessModelTable pm
						LEFT JOIN FETCH pm.events
						WHERE pm.project=:project
						""", ProcessModelTable.class)//
				.setParameter("project", projectTable)//
				.getResultList();

		processModels = em//
				.createQuery("""
						SELECT DISTINCT pm FROM ProcessModelTable pm
						LEFT JOIN FETCH pm.callActivites
						WHERE pm in :processModels
						""", ProcessModelTable.class)//
				.setParameter("processModels", processModels)//
				.getResultList();

		return processModels;
	}

	@Transactional
	public List<ProcessModelTable> getProcessModelsForName(String name, ProjectTable projectTable) {
		List<ProcessModelTable> processModels = em//
				.createQuery("""
						SELECT pm FROM ProcessModelTable pm
						LEFT JOIN FETCH pm.events
						WHERE pm.name = :name AND pm.project =:project
						""", ProcessModelTable.class)//
				.setParameter("name", name)//
				.setParameter("project", projectTable)//
				.getResultList();

		processModels = em//
				.createQuery("""
						SELECT DISTINCT pm FROM ProcessModelTable pm
						LEFT JOIN FETCH pm.callActivites
						WHERE pm in :processModels
						""", ProcessModelTable.class)//
				.setParameter("processModels", processModels)//
				.getResultList();

		return processModels;
	}

	@Transactional
	public void persist(ProcessModelTable table) {
		em.persist(table);
	}

	@Transactional
	public void merge(ProcessModelTable table) {
		em.merge(table);
	}

	@Transactional
	public ProcessModelTable findWithChildren(Long id) {

		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);
		graph.addSubgraph("events");
		graph.addSubgraph("callActivites");

		Map<String, Object> hints = new HashMap<String, Object>();
		hints.put("javax.persistence.loadgraph", graph);

		return em.find(ProcessModelTable.class, id, hints);
	}

	@Transactional
	public ProcessModelTable find(Long id) {
		return em.find(ProcessModelTable.class, id);
	}

	@Transactional
	public ProcessModelTable findByBpmnProcessId(String bpmnProcessId, ProjectTable projectTable) {
		List<ProcessModelTable> processModels = em //
				.createQuery("SELECT p FROM ProcessModelTable p " + //
						"WHERE p.bpmnProcessId = :bpmnProcessId AND p.project = :project", ProcessModelTable.class)
				.setParameter("bpmnProcessId", bpmnProcessId) //
				.setParameter("project", projectTable) //
				.getResultList();

		return !processModels.isEmpty() ? processModels.getFirst() : null;
	}

	@Transactional
	public void delete(Long id) {
		ProcessModelTable table = em.find(ProcessModelTable.class, id);
		em.remove(table);
	}

	@Transactional
	public void delete(List<Long> processModelIds) {
		processModelIds.forEach(this::delete);
	}

	@Transactional
	public ProcessModelTable findByName(String name, ProjectTable projectTable) {
		List<ProcessModelTable> processModels = em //
				.createQuery("SELECT p FROM ProcessModelTable p " + //
						"WHERE p.name = :name AND p.project = :project", ProcessModelTable.class)
				.setParameter("name", name) //
				.setParameter("project", projectTable) //
				.getResultList();
		return processModels.isEmpty() ? null : processModels.getFirst();
	}
}
