package de.envite.proa.repository.processmodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

@RequestScoped
public class ProcessModelDao {

	private final EntityManager em;
	private static final String FETCH_GRAPH = "jakarta.persistence.fetchgraph";
	private static final String LOAD_GRAPH = "jakarta.persistence.loadgraph";
	private static final String PROCESS_MODELS = "processModels";
	private static final String PROJECT = "project";
	private static final String SELECT_FROM_PROCESS_MODEL_TABLE = "SELECT p FROM ProcessModelTable p ";

	@Inject
	public ProcessModelDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public List<ProcessModelTable> getProcessModelsWithParentsAndChildren(ProjectTable projectTable, List<Integer> levels) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);
		graph.addSubgraph("events");

		String queryString = """
						SELECT pm FROM ProcessModelTable pm
						WHERE pm.project=:project
						""";

		if (levels != null && !levels.isEmpty()) {
			queryString += " AND pm.level IN :levels";
		}

        queryString += " ORDER BY pm.name ASC";

		TypedQuery<ProcessModelTable> query = em//
				.createQuery(queryString, ProcessModelTable.class)//
				.setParameter(PROJECT, projectTable)//
				.setHint(LOAD_GRAPH, graph);

		if (levels != null && !levels.isEmpty()) {
			query.setParameter("levels", levels);
		}

		return query.getResultList();
	}

	@Transactional
	public List<ProcessModelTable> getProcessModels(ProjectTable projectTable) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		List<ProcessModelTable> processModels = em//
				.createQuery("""
						SELECT pm FROM ProcessModelTable pm
						LEFT JOIN FETCH pm.events
						WHERE pm.project=:project
						""", ProcessModelTable.class)//
				.setParameter(PROJECT, projectTable)//
				.setHint(FETCH_GRAPH, graph)//
				.getResultList();

		processModels = em//
				.createQuery("""
						SELECT DISTINCT pm FROM ProcessModelTable pm
						LEFT JOIN FETCH pm.callActivites
						WHERE pm in :processModels
						""", ProcessModelTable.class)//
				.setParameter(PROCESS_MODELS, processModels)//
				.setHint(FETCH_GRAPH, graph)//
				.getResultList();

		return processModels;
	}

	@Transactional
	public ProcessModelTable getProcessModelById(ProjectTable projectTable, Long id) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		return em//
				.createQuery("""
                    SELECT pm FROM ProcessModelTable pm
                    WHERE pm.project = :project AND pm.id = :id
                    """, ProcessModelTable.class)//
				.setParameter(PROJECT, projectTable)//
				.setParameter("id", id)//
				.setHint(LOAD_GRAPH, graph)//
				.getSingleResult();
	}

	@Transactional
	public List<ProcessModelTable> getProcessModelsByIds(ProjectTable projectTable, List<Long> ids) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		return em//
				.createQuery("""
                 SELECT pm FROM ProcessModelTable pm
                 WHERE pm.project = :project AND pm.id IN :ids
                 """, ProcessModelTable.class)//
				.setParameter(PROJECT, projectTable)//
				.setParameter("ids", ids)//
				.setHint(FETCH_GRAPH, graph)//
				.getResultList();
	}

	@Transactional
	public List<ProcessModelTable> getProcessModelsForName(String name, ProjectTable projectTable) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		List<ProcessModelTable> processModels = em//
				.createQuery("""
						SELECT pm FROM ProcessModelTable pm
						LEFT JOIN FETCH pm.events
						WHERE pm.name = :name AND pm.project =:project
						""", ProcessModelTable.class)//
				.setParameter("name", name)//
				.setParameter(PROJECT, projectTable)//
				.setHint(FETCH_GRAPH, graph)//
				.getResultList();

		processModels = em//
				.createQuery("""
						SELECT DISTINCT pm FROM ProcessModelTable pm
						LEFT JOIN FETCH pm.callActivites
						WHERE pm in :processModels
						""", ProcessModelTable.class)//
				.setParameter(PROCESS_MODELS, processModels)//
				.setHint(FETCH_GRAPH, graph)//
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

		Map<String, Object> hints = new HashMap<>();
		hints.put(FETCH_GRAPH, graph);

		return em.find(ProcessModelTable.class, id, hints);
	}

	@Transactional
	public ProcessModelTable findWithParentsAndChildren(Long id) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		Map<String, Object> hints = new HashMap<>();
		hints.put(LOAD_GRAPH, graph);

		return em.find(ProcessModelTable.class, id, hints);
	}

	@Transactional
	public List<ProcessModelTable> findWithRelatedProcessModels(Long id) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		return em//
				.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE + //
						"LEFT JOIN FETCH p.relatedProcessModels r " + //
						"WHERE p.id = :id OR r.relatedProcessModelId = :id", ProcessModelTable.class)
				.setParameter("id", id)//
				.setHint(FETCH_GRAPH, graph)//
				.getResultList();
	}

	@Transactional
	public List<ProcessEventTable> findEventsForProcessModels(List<ProcessModelTable> processModels) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		return em//
				.createQuery("SELECT e FROM ProcessEventTable e " + //
						"WHERE e.processModel IN :processModels", ProcessEventTable.class)//
				.setParameter(PROCESS_MODELS, processModels)//
				.setHint(FETCH_GRAPH, graph)//
				.getResultList();
	}

	@Transactional
	public List<CallActivityTable> findCallActivitiesForProcessModels(List<ProcessModelTable> processModels) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		return em//
				.createQuery("SELECT ca FROM CallActivityTable ca " + //
						"WHERE ca.processModel IN :processModels", CallActivityTable.class)//
				.setParameter(PROCESS_MODELS, processModels)//
				.setHint(FETCH_GRAPH, graph)//
				.getResultList();
	}

	@Transactional
	public ProcessModelTable find(Long id) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);
        graph.addSubgraph("parents");

		Map<String, Object> hints = new HashMap<>();
		hints.put(FETCH_GRAPH, graph);

		return em.find(ProcessModelTable.class, id, hints);
	}

	@Transactional
	public ProcessModelTable findByBpmnProcessId(String bpmnProcessId, ProjectTable projectTable) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		List<ProcessModelTable> processModels = em//
				.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE + //
						"WHERE p.bpmnProcessId = :bpmnProcessId AND p.project = :project", ProcessModelTable.class)
				.setParameter("bpmnProcessId", bpmnProcessId)//
				.setParameter(PROJECT, projectTable)//
				.setHint(FETCH_GRAPH, graph)//
				.getResultList();

		return !processModels.isEmpty() ? processModels.getFirst() : null;
	}

	@Transactional
	public ProcessModelTable findByBpmnProcessIdWithChildren(String bpmnProcessId, ProjectTable projectTable) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);
		graph.addSubgraph("children");

		List<ProcessModelTable> processModels = em//
				.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE + //
						"WHERE p.bpmnProcessId = :bpmnProcessId AND p.project = :project", ProcessModelTable.class)
				.setParameter("bpmnProcessId", bpmnProcessId)//
				.setParameter(PROJECT, projectTable)//
				.setHint(LOAD_GRAPH, graph)//
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
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		List<ProcessModelTable> processModels = em//
				.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE + //
						"WHERE p.name = :name AND p.project = :project", ProcessModelTable.class)
				.setParameter("name", name)//
				.setParameter(PROJECT, projectTable)//
				.setHint(FETCH_GRAPH, graph)//
				.getResultList();
		return processModels.isEmpty() ? null : processModels.getFirst();
	}
}
