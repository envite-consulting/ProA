package de.envite.proa.repository;

import java.util.List;

import de.envite.proa.entities.DataStoreConnectionWithoutAccess;
import de.envite.proa.entities.ProcessConnection;
import de.envite.proa.entities.ProcessElementType;
import de.envite.proa.repository.tables.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

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
		return em//
				.createQuery("SELECT pc FROM ProcessConnectionTable pc WHERE pc.project = :project",
						ProcessConnectionTable.class)//
				.setParameter("project", projectTable).getResultList();
	}

	@Transactional
	public void deleteForProcessModel(Long id) {

		ProcessModelTable processModel = em.find(ProcessModelTable.class, id);
		em.createQuery(
				"DELETE FROM ProcessConnectionTable pc WHERE pc.callingProcess = :processModel OR pc.calledProcess = :processModel")
				.setParameter("processModel", processModel)//
				.executeUpdate();
	}

	@Transactional
	public Response addConnection(Long projectId, ProcessConnection connection) {
		ProcessConnectionTable processConnection = map(projectId, connection);
		em.persist(processConnection);
		return Response.ok().build();
	}

	@Transactional
	public Response deleteConnection(Long projectId, ProcessConnection connection) {
		ProcessConnectionTable processConnection = map(projectId, connection);
		em.createQuery("DELETE FROM ProcessConnectionTable pc " + "WHERE pc.callingProcess = :callingProcess "
				+ "AND pc.calledProcess = :calledProcess " + "AND pc.callingElementType = :callingElementType "
				+ "AND pc.calledElementType = :calledElementType " + "AND pc.project = :project")
				.setParameter("callingProcess", processConnection.getCallingProcess())
				.setParameter("calledProcess", processConnection.getCalledProcess())
				.setParameter("callingElementType", processConnection.getCallingElementType())
				.setParameter("calledElementType", processConnection.getCalledElementType())
				.setParameter("project", processConnection.getProject()).executeUpdate();

		return Response.ok().build();
	}

	@Transactional
	public Response deleteConnection(Long projectId, DataStoreConnectionWithoutAccess connection) {
		DataStoreConnectionTable dataStoreConnection = map(projectId, connection);
		em.createQuery("DELETE FROM DataStoreConnectionTable pc " + "WHERE pc.process = :process "
				+ "AND pc.dataStore = :dataStore " + "AND pc.project = :project ")
				.setParameter("process", dataStoreConnection.getProcess())
				.setParameter("dataStore", dataStoreConnection.getDataStore())
				.setParameter("project", dataStoreConnection.getProject()).executeUpdate();

		return Response.ok().build();
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
					.createQuery("SELECT cat.elementId FROM CallActivityTable cat "
							+ "WHERE cat.processModel = :processModel", String.class)
					.setParameter("processModel", processModel).getResultList().stream().findFirst().orElse(null);
		}
		return em
				.createQuery("SELECT pe.elementId FROM ProcessEventTable pe " + "WHERE pe.processModel = :processModel",
						String.class)
				.setParameter("processModel", processModel).getResultList().stream().findFirst().orElse(null);
	}

	private DataStoreConnectionTable map(Long projectId, DataStoreConnectionWithoutAccess connection) {
		DataStoreConnectionTable table = new DataStoreConnectionTable();
		table.setProcess(em.find(ProcessModelTable.class, connection.getProcessid()));
		table.setDataStore(em.find(DataStoreTable.class, connection.getDataStoreId()));
		table.setProject(em.find(ProjectTable.class, projectId));
		return table;
	}
}