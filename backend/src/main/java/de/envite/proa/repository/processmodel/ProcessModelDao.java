package de.envite.proa.repository.processmodel;

import de.envite.proa.entities.process.ProcessType;
import de.envite.proa.repository.tables.ProcessModelTable;
import de.envite.proa.repository.tables.ProjectTable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@RequestScoped
public class ProcessModelDao {

	private EntityManager em;

	@Inject
	public ProcessModelDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public List<ProcessModelTable> getProcessModelsWithChildren(ProjectTable projectTable) {
		EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withChildren");
		String query = "SELECT p " +
				"FROM ProcessModelTable p " +
				"WHERE p.project = :project";
		return em.createQuery(query, ProcessModelTable.class)
				.setParameter("project", projectTable)
				.setHint("javax.persistence.loadgraph", graph)
				.getResultList();
	}

	@Transactional
	public List<ProcessModelTable> getProcessModelsWithoutCollaborationsAndWithEventsAndActivities(
			ProjectTable projectTable) {
		EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withEventsAndActivities");
		String query = "SELECT p " +
				"FROM ProcessModelTable p " +
				"WHERE p.project = :project " +
				"AND p.processType != :collaboration ";
		return em.createQuery(query, ProcessModelTable.class)
				.setParameter("collaboration", ProcessType.COLLABORATION)
				.setParameter("project", projectTable)
				.setHint("javax.persistence.loadgraph", graph)
				.getResultList();
	}

	@Transactional
	public List<ProcessModelTable> getProcessModelsForName(String name, ProjectTable projectTable) {
		return em
				.createQuery(
						"SELECT p " +
								"FROM ProcessModelTable p " +
								"WHERE p.name = :name " +
								"AND p.project = :project",
						ProcessModelTable.class)
				.setParameter("name", name)
				.setParameter("project", projectTable)
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
		String query = "SELECT p " +
				"FROM ProcessModelTable p " +
				"WHERE p.id = :id";
		return em.createQuery(query, ProcessModelTable.class)
				.setParameter("id", id)
				.setHint("javax.persistence.loadgraph", graph)
				.getSingleResult();
	}

	@Transactional
	public ProcessModelTable findWithChildren(Long id) {
		EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withChildren");
		String query = "SELECT p " +
				"FROM ProcessModelTable p " +
				"WHERE p.id = :id";
		return em.createQuery(query, ProcessModelTable.class)
				.setParameter("id", id)
				.setHint("javax.persistence.loadgraph", graph)
				.getSingleResult();
	}

	@Transactional
	public ProcessModelTable findWithParents(Long id) {
		EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withParents");
		String query = "SELECT p " +
				"FROM ProcessModelTable p " +
				"WHERE p.id = :id";
		return em.createQuery(query, ProcessModelTable.class)
				.setParameter("id", id)
				.setHint("javax.persistence.loadgraph", graph)
				.getSingleResult();
	}

	@Transactional
	public ProcessModelTable find(Long id) {
		return em.find(ProcessModelTable.class, id);
	}

	@Transactional
	public ProcessModelTable findWithEventsAndActivities(Long id) {
		EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withEventsAndActivities");
		String query = "SELECT p " +
				"FROM ProcessModelTable p " +
				"WHERE p.id = :id";
		return em.createQuery(query, ProcessModelTable.class)
				.setParameter("id", id)
				.setHint("javax.persistence.loadgraph", graph)
				.getSingleResult();
	}

	@Transactional
	public ProcessModelTable findByBpmnProcessId(String bpmnProcessId, ProjectTable projectTable) {
		List<ProcessModelTable> processModels = em
				.createQuery(
						"SELECT p " +
								"FROM ProcessModelTable p " +
								"WHERE p.bpmnProcessId = :bpmnProcessId " +
								"AND p.project = :project",
						ProcessModelTable.class
				)
				.setParameter("bpmnProcessId", bpmnProcessId)
				.setParameter("project", projectTable)
				.getResultList();

		return !processModels.isEmpty() ? processModels.getFirst() : null;
	}

	@Transactional
	public ProcessModelTable findByBpmnProcessIdWithChildren(String bpmnProcessId, ProjectTable projectTable) {
		EntityGraph<?> graph = em.getEntityGraph("ProcessModel.withChildren");
		List<ProcessModelTable> processModels = em
				.createQuery(
						"SELECT p " +
								"FROM ProcessModelTable p " +
								"WHERE p.bpmnProcessId = :bpmnProcessId " +
								"AND p.project = :project",
						ProcessModelTable.class
				)
				.setParameter("bpmnProcessId", bpmnProcessId)
				.setParameter("project", projectTable)
				.setHint("javax.persistence.loadgraph", graph)
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

		List<ProcessModelTable> processModels = em
				.createQuery(
						"SELECT p " +
								"FROM ProcessModelTable p " +
								"WHERE p.name = :name AND p.project = :project",
						ProcessModelTable.class
				)
				.setParameter("name", name)
				.setParameter("project", projectTable)
				.getResultList();

		return processModels.isEmpty() ? null : processModels.getFirst();
	}
}
