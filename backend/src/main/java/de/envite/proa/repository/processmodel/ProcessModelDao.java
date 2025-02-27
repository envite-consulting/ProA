package de.envite.proa.repository.processmodel;

import de.envite.proa.entities.process.ProcessType;
import de.envite.proa.repository.tables.CallActivityTable;
import de.envite.proa.repository.tables.ProcessEventTable;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.*;

@RequestScoped
public class ProcessModelDao {

	private final EntityManager em;
	private static final String FETCH_GRAPH = "jakarta.persistence.fetchgraph";
	private static final String LOAD_GRAPH = "jakarta.persistence.loadgraph";
	private static final String PROJECT = "project";
	private static final String PROCESS_MODELS = "processModels";
	private static final String SELECT_FROM_PROCESS_MODEL_TABLE_WHERE = "SELECT p FROM ProcessModelTable p WHERE ";
	private static final String ID_ID = "p.id = :id";
	private static final String PROJECT_PROJECT = "p.project = :project";
	private static final String PROCESS_MODEL_WITH_CHILDREN = "ProcessModel.withChildren";

	@Inject
	public ProcessModelDao(EntityManager em) {
		this.em = em;
	}

    @Transactional
    public List<ProcessModelTable> getProcessModelsWithChildren(ProjectTable projectTable) {
        EntityGraph<?> graph = em.getEntityGraph(PROCESS_MODEL_WITH_CHILDREN);

		return em.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
						PROJECT_PROJECT + " ORDER BY p.name ASC", ProcessModelTable.class)
                .setParameter(PROJECT, projectTable)
                .setHint(LOAD_GRAPH, graph)
                .getResultList();
    }

	@Transactional
	public List<ProcessModelTable> getProcessModelsWithParentsAndEvents(ProjectTable projectTable) {
		EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withParentsAndEvents");

		return em.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
						PROJECT_PROJECT, ProcessModelTable.class)
				.setParameter(PROJECT, projectTable)
				.setHint(LOAD_GRAPH, graph)
				.getResultList();
	}

    @Transactional
    public List<ProcessModelTable> getProcessModelsWithoutCollaborationsAndWithEventsAndActivities(
            ProjectTable projectTable) {
        EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withEventsAndActivities");

        return em.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
						PROJECT_PROJECT +
						" AND (p.processType != :collaboration OR p.processType IS NULL)", ProcessModelTable.class)
                .setParameter("collaboration", ProcessType.COLLABORATION)
                .setParameter(PROJECT, projectTable)
                .setHint(LOAD_GRAPH, graph)
                .getResultList();
    }

	@Transactional
	public ProcessModelTable getProcessModelById(ProjectTable projectTable, Long id) {
		EntityGraph<?> graph = em.getEntityGraph(PROCESS_MODEL_WITH_CHILDREN);

		return em.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
						PROJECT_PROJECT + " AND " + ID_ID, ProcessModelTable.class)
				.setParameter(PROJECT, projectTable)
				.setParameter("id", id)
				.setHint(LOAD_GRAPH, graph)
				.getSingleResult();
	}

	@Transactional
	public List<ProcessModelTable> getProcessModelsByIds(ProjectTable projectTable, List<Long> ids) {
		EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withParents");

		return em.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
						PROJECT_PROJECT + " AND p.id IN :ids", ProcessModelTable.class)
				.setParameter(PROJECT, projectTable)
				.setParameter("ids", ids)
				.setHint(LOAD_GRAPH, graph)
				.getResultList();
	}

	@Transactional
	public List<ProcessModelTable> getProcessModelsForName(String name, ProjectTable projectTable) {
		return em
				.createQuery(
						SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
								"p.name = :name " +
								"AND " + PROJECT_PROJECT,
						ProcessModelTable.class)
				.setParameter("name", name)
				.setParameter(PROJECT, projectTable)
				.getResultList();
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
	public ProcessModelTable findWithParentsAndChildren(Long id) {
		EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withChildrenAndParents");

        return em.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
						ID_ID, ProcessModelTable.class)
				.setParameter("id", id)
				.setHint(LOAD_GRAPH, graph)
				.getSingleResult();
	}

	@Transactional
	public ProcessModelTable findWithChildren(Long id) {
		EntityGraph<?> graph = em.getEntityGraph(PROCESS_MODEL_WITH_CHILDREN);

		return em.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
						ID_ID, ProcessModelTable.class)
				.setParameter("id", id)
				.setHint(LOAD_GRAPH, graph)
				.getSingleResult();
	}

	@Transactional
	public ProcessModelTable findWithParents(Long id) {
		EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withParents");

		return em.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
						ID_ID, ProcessModelTable.class)
				.setParameter("id", id)
				.setHint(LOAD_GRAPH, graph)
				.getSingleResult();
	}

	@Transactional
	public Set<ProcessModelTable> findWithRelatedProcessModels(Long id) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		return new HashSet<>(em.createQuery("SELECT p FROM ProcessModelTable p " +
						"LEFT JOIN FETCH p.relatedProcessModels r " +
						"WHERE p.id = :id OR r.relatedProcessModelId = :id", ProcessModelTable.class)
				.setParameter("id", id)
				.setHint(FETCH_GRAPH, graph)
				.getResultList());
	}

	@Transactional
	public Set<ProcessEventTable> findEventsForProcessModels(Set<ProcessModelTable> processModels) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		return new HashSet<>(em.createQuery("SELECT e FROM ProcessEventTable e " +
						"WHERE e.processModel IN :processModels", ProcessEventTable.class)
				.setParameter(PROCESS_MODELS, processModels)
				.setHint(FETCH_GRAPH, graph)
				.getResultList());
	}

	@Transactional
	public Set<CallActivityTable> findCallActivitiesForProcessModels(Set<ProcessModelTable> processModels) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		return new HashSet<>(em.createQuery("SELECT ca FROM CallActivityTable ca " +
						"WHERE ca.processModel IN :processModels", CallActivityTable.class)
				.setParameter(PROCESS_MODELS, processModels)
				.setHint(FETCH_GRAPH, graph)
				.getResultList());
	}

	@Transactional
	public ProcessModelTable find(Long id) {
		return em.find(ProcessModelTable.class, id);
	}

	@Transactional
	public ProcessModelTable findWithEventsAndActivities(Long id) {
		EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withEventsAndActivities");

		return em.createQuery(SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
						ID_ID, ProcessModelTable.class)
				.setParameter("id", id)
				.setHint(LOAD_GRAPH, graph)
				.getSingleResult();
	}

	@Transactional
	public byte[] getBpmnXml(Long id) {
		EntityGraph<ProcessModelTable> graph = em.createEntityGraph(ProcessModelTable.class);

		Map<String, Object> hints = new HashMap<>();
		hints.put(FETCH_GRAPH, graph);

		return em.find(ProcessModelTable.class, id, hints).getBpmnXml();
	}

	@Transactional
	public ProcessModelTable findByBpmnProcessIdWithChildren(String bpmnProcessId, ProjectTable projectTable) {
		EntityGraph<?> graph = em.getEntityGraph(PROCESS_MODEL_WITH_CHILDREN);

        List<ProcessModelTable> processModels = em
				.createQuery(
						SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
								"p.bpmnProcessId = :bpmnProcessId " +
								"AND " + PROJECT_PROJECT,
						ProcessModelTable.class
				)
				.setParameter("bpmnProcessId", bpmnProcessId)
				.setParameter(PROJECT, projectTable)
				.setHint(LOAD_GRAPH, graph)
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
	public ProcessModelTable findByNameOrBpmnProcessIdWithoutCollaborations(String name, String bpmnProcessId,
			ProjectTable projectTable) {
		List<ProcessModelTable> processModels = em.createQuery(
				SELECT_FROM_PROCESS_MODEL_TABLE_WHERE +
						"(p.name = :name OR p.bpmnProcessId = :bpmnProcessId) " +
						"AND (p.processType != :collaboration OR p.processType IS NULL) " +
						"AND " + PROJECT_PROJECT, ProcessModelTable.class)
				.setParameter("name", name)
				.setParameter("bpmnProcessId", bpmnProcessId)
				.setParameter("collaboration", ProcessType.COLLABORATION)
				.setParameter(PROJECT, projectTable)
				.getResultList();

		return processModels.isEmpty() ? null : processModels.getFirst();
	}

	@Transactional
	public void addChild(Long parentId, Long childId) {
		ProcessModelTable parent = em.find(ProcessModelTable.class, parentId);
		ProcessModelTable child = em.find(ProcessModelTable.class, childId);
		parent.getChildren().add(child);
		em.merge(parent);
	}

	@Transactional
	public void removeChild(Long parentId, Long childId) {
		ProcessModelTable parent = em.find(ProcessModelTable.class, parentId);
		ProcessModelTable child = em.find(ProcessModelTable.class, childId);
		parent.getChildren().remove(child);
		em.merge(parent);
	}
}
