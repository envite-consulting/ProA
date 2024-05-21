package de.envite.proa.repository;

import java.util.List;

import de.envite.proa.entities.DataStoreConnection;
import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessElementType;
import de.envite.proa.repository.tables.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProcessConnectionDao {

	private EntityManager em;

	@Inject
	public ProcessConnectionDao(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public void persist(ProcessConnectionTable table) {
		em.persist(table);
	}

	@Transactional
	public List<ProcessConnectionTable> getProcessConnections(ProjectTable projectTable) {
		return em
				.createQuery("SELECT pc FROM ProcessConnectionTable pc WHERE pc.project = :project",
						ProcessConnectionTable.class)
				.setParameter("project", projectTable)
				.getResultList();
	}

	@Transactional
	public void deleteForProcessModel(Long id) {

		ProcessModelTable processModel = em.find(ProcessModelTable.class, id);
		em.createQuery(
				"DELETE FROM ProcessConnectionTable pc WHERE pc.callingProcess = :processModel OR pc.calledProcess = :processModel")
				.setParameter("processModel", processModel)
				.executeUpdate();
	}

	@Transactional
	public void addConnection(Long projectId, ProcessConnection connection) {
		ProcessConnectionTable connectionTable = map(projectId, connection);
		em.persist(connectionTable);
	}

	@Transactional
	public void deleteConnection(Long projectId, ProcessConnection connection) {
		ProcessConnectionTable connectionTable = map(projectId, connection);
		em.createQuery("DELETE FROM ProcessConnectionTable pc " +
				"WHERE pc.callingProcess = :callingProcess " +
				"AND pc.calledProcess = :calledProcess " +
				"AND pc.callingElementType = :callingElementType " +
				"AND pc.calledElementType = :calledElementType " +
				"AND pc.project = :project")
				.setParameter("callingProcess", connectionTable.getCallingProcess())
				.setParameter("calledProcess", connectionTable.getCalledProcess())
				.setParameter("callingElementType", connectionTable.getCallingElementType())
				.setParameter("calledElementType", connectionTable.getCalledElementType())
				.setParameter("project", connectionTable.getProject())
				.executeUpdate();
	}

	@Transactional
	public void deleteConnection(Long projectId, DataStoreConnection connection) {
		DataStoreConnectionTable dataStoreConnection = map(projectId, connection);
		em.createQuery("DELETE FROM DataStoreConnectionTable pc " +
				"WHERE pc.process = :process " +
				"AND pc.dataStore = :dataStore " +
				"AND pc.project = :project ")
				.setParameter("process", dataStoreConnection.getProcess())
				.setParameter("dataStore", dataStoreConnection.getDataStore())
				.setParameter("project", dataStoreConnection.getProject())
				.executeUpdate();
	}

	private ProcessConnectionTable map(Long projectId, ProcessConnection connection) {
		ProcessElementType callingElementType = connection.getCallingElementType();
		ProcessElementType calledElementType = connection.getCalledElementType();
		ProcessModelTable callingProcess = em.find(ProcessModelTable.class, connection.getCallingProcessid());
		ProcessModelTable calledProcess = em.find(ProcessModelTable.class, connection.getCalledProcessid());
		String callingElement = getElementId(callingElementType, callingProcess);
		String calledElement = getElementId(calledElementType, calledProcess);

		ProcessConnectionTable table = new ProcessConnectionTable();
		table.setCallingProcess(callingProcess);
		table.setCalledProcess(calledProcess);
		table.setCallingElementType(callingElementType);
		table.setCalledElementType(calledElementType);
		table.setCallingElement(callingElement);
		table.setCalledElement(calledElement);
		table.setProject(em.find(ProjectTable.class, projectId));
		return table;
	}

	private String getElementId(ProcessElementType elementType, ProcessModelTable processModel) {
		if (elementType == ProcessElementType.CALL_ACTIVITY) {
			return em
					.createQuery("SELECT cat.elementId FROM CallActivityTable cat " +
							"WHERE cat.processModel = :processModel",
							String.class)
					.setParameter("processModel", processModel)
					.getResultList()
					.stream()
					.findFirst()
					.get();
		}
		return em
				.createQuery("SELECT pe.elementId FROM ProcessEventTable pe " +
						"WHERE pe.processModel = :processModel",
						String.class)
				.setParameter("processModel", processModel)
				.getResultList()
				.stream()
				.findFirst()
				.get();
	}

	private DataStoreConnectionTable map(Long projectId, DataStoreConnection connection) {
		DataStoreConnectionTable table = new DataStoreConnectionTable();
		table.setProcess(em.find(ProcessModelTable.class, connection.getProcessid()));
		table.setDataStore(em.find(DataStoreTable.class, connection.getDataStoreId()));
		table.setProject(em.find(ProjectTable.class, projectId));
		table.setAccess(null);
		return table;
	}
}