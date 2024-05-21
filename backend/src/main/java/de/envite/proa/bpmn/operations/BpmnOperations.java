package de.envite.proa.bpmn.operations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.SourceRef;
import org.camunda.bpm.model.bpmn.impl.instance.TargetRef;
import org.camunda.bpm.model.bpmn.instance.CallActivity;
import org.camunda.bpm.model.bpmn.instance.DataAssociation;
import org.camunda.bpm.model.bpmn.instance.DataStoreReference;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.Event;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import de.envite.proa.entities.DataAccess;
import de.envite.proa.entities.EventType;
import de.envite.proa.entities.ProcessActivity;
import de.envite.proa.entities.ProcessDataStore;
import de.envite.proa.entities.ProcessEvent;
import de.envite.proa.usecases.ProcessOperations;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BpmnOperations implements ProcessOperations {

	@Override
	public List<ProcessEvent> getStartEvents(String processModel) {
		return getEvents(processModel, EventType.START, StartEvent.class);
	}

	@Override
	public List<ProcessEvent> getIntermediateThrowEvents(String processModel) {
		return getEvents(processModel, EventType.INTERMEDIATE_THROW, IntermediateThrowEvent.class);
	}

	@Override
	public List<ProcessEvent> getIntermediateCatchEvents(String processModel) {
		return getEvents(processModel, EventType.INTERMEDIATE_CATCH, IntermediateCatchEvent.class);
	}

	@Override
	public List<ProcessEvent> getEndEvents(String processModel) {
		return getEvents(processModel, EventType.END, EndEvent.class);
	}

	@Override
	public List<ProcessActivity> getCallActivities(String processModel) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		Collection<CallActivity> callActivities = processModelInstance.getModelElementsByType(CallActivity.class);

		return callActivities//
				.stream()//
				.map(activity -> new ProcessActivity(activity.getId(), activity.getName()))//
				.collect(Collectors.toList());
	}

	private <T extends Event> List<ProcessEvent> getEvents(String processModel, EventType eventType,
			Class<T> eventClass) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		Collection<T> startEvents = processModelInstance.getModelElementsByType(eventClass);
		return startEvents//
				.stream()//
				.map(event -> new ProcessEvent(event.getId(), event.getName(), eventType))//
				.collect(Collectors.toList());
	}

	private BpmnModelInstance getProcessModelInstance(String processModel) {
		InputStream processModelStream = new ByteArrayInputStream(processModel.getBytes(StandardCharsets.UTF_8));
		return Bpmn.readModelFromStream(processModelStream);
	}

	@Override
	public List<ProcessDataStore> getDataStores(String processModel) {
		BpmnModelInstance processModelInstance = getProcessModelInstance(processModel);

		Collection<DataStoreReference> dataStores = processModelInstance
				.getModelElementsByType(DataStoreReference.class);

		Collection<DataAssociation> dataAssociations = processModelInstance
				.getModelElementsByType(DataAssociation.class);

		return dataStores//
				.stream()//
				.map(store -> {

					DataAccess access = getDataStoreAccess(dataAssociations, store);
					ProcessDataStore processDataStore = new ProcessDataStore(store.getId(), store.getName(), access);
					return processDataStore;
				})//
				.collect(Collectors.toList());
	}

	private DataAccess getDataStoreAccess(Collection<DataAssociation> dataAssociations, DataStoreReference store) {
		boolean isStoreTarget = dataAssociations//
				.stream()//
				.flatMap(association -> association.getChildElementsByType(TargetRef.class).stream())//
				.anyMatch(target -> target.getRawTextContent().equals(store.getId()));

		boolean isStoreSource = dataAssociations//
				.stream()//
				.flatMap(association -> association.getChildElementsByType(SourceRef.class).stream())//
				.anyMatch(target -> target.getRawTextContent().equals(store.getId()));

		DataAccess access = null;
		if (isStoreSource && isStoreTarget) {
			access = DataAccess.READ_WRITE;
		} else if (isStoreSource) {
			access = DataAccess.READ;
		} else if (isStoreTarget) {
			access = DataAccess.WRITE;
		} else {
			access = DataAccess.NONE;
		}
		return access;
	}
}