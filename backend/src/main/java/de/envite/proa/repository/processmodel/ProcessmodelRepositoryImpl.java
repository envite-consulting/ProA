package de.envite.proa.repository.processmodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.envite.proa.XmlConverter;
import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.entities.process.*;
import de.envite.proa.repository.datastore.DataStoreConnectionDao;
import de.envite.proa.repository.datastore.DataStoreDao;
import de.envite.proa.repository.messageflow.MessageFlowDao;
import de.envite.proa.repository.messageflow.MessageFlowMapper;
import de.envite.proa.repository.tables.*;
import de.envite.proa.usecases.processmodel.ProcessModelRepository;
import de.envite.proa.usecases.processmodel.RelatedProcessModelRepository;
import de.envite.proa.usecases.settings.SettingsRepository;
import io.vertx.ext.web.handler.sockjs.impl.StringEscapeUtils;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@RequestScoped
public class ProcessmodelRepositoryImpl implements ProcessModelRepository {

    private final ProcessModelDao processModelDao;
    private final DataStoreDao dataStoreDao;
    private final DataStoreConnectionDao dataStoreConnectionDao;
    private final CallActivityDao callActivityDao;
    private final ProcessConnectionDao processConnectionDao;
    private final ProcessEventDao processEventDao;
    private final MessageFlowDao messageFlowDao;
    private final RelatedProcessModelDao relatedProcessModelDao;
    private final RelatedProcessModelRepository relatedProcessModelRepository;
	private final SettingsRepository settingsRepository;
	private static final String CONTENTS = "contents";
	private static final String PARTS = "parts";
	private static final String TEXT = "text";
	Logger logger = Logger.getLogger(getClass().getName());

    @Inject
    public ProcessmodelRepositoryImpl(ProcessModelDao processModelDao, //
                                      DataStoreDao dataStoreDao, //
                                      DataStoreConnectionDao dataStoreConnectionDao, //
                                      CallActivityDao callActivityDao, //
                                      ProcessConnectionDao processConnectionDao, //
                                      ProcessEventDao processEventDao, //
                                      MessageFlowDao messageFlowDao, //
                                      RelatedProcessModelDao relatedProcessModelDao, //
                                      RelatedProcessModelRepository relatedProcessModelRepository, //
									  SettingsRepository settingsRepository) {
        this.processModelDao = processModelDao;
        this.dataStoreDao = dataStoreDao;
        this.dataStoreConnectionDao = dataStoreConnectionDao;
        this.callActivityDao = callActivityDao;
        this.processConnectionDao = processConnectionDao;
        this.processEventDao = processEventDao;
        this.messageFlowDao = messageFlowDao;
        this.relatedProcessModelDao = relatedProcessModelDao;
        this.relatedProcessModelRepository = relatedProcessModelRepository;
		this.settingsRepository = settingsRepository;
    }

	@Override
	public Long saveProcessModel(Long projectId, ProcessModel processModel) {
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(projectId);

		ProcessModelTable table = ProcessmodelMapper.map(processModel, projectTable);
		table.setCreatedAt(LocalDateTime.now());
		table.setProject(projectTable);

		String parentBpmnProcessId = processModel.getParentBpmnProcessId();
		processModelDao.persist(table);
		if (parentBpmnProcessId != null) {
			ProcessModelTable parent = processModelDao.findByBpmnProcessIdWithChildren(parentBpmnProcessId,
					projectTable);
			processModelDao.addChild(parent.getId(), table.getId());
		}
		
        relatedProcessModelRepository.calculateAndSaveRelatedProcessModels(projectTable);

		connectEvents(processModel, table, projectTable);

		connectCallActivities(processModel, table, projectTable);

		connectDataStores(table, projectTable);

		return table.getId();
	}

    @Override
    public void addRelatedProcessModel(Long projectId, Long id, List<Long> relatedProcessModelIds) {
        relatedProcessModelRepository.addRelatedProcessModel(projectId, id, relatedProcessModelIds);
    }

    @Override
    public List<ProcessInformation> getProcessInformation(Long projectId, String levelParam) {
        ProjectTable projectTable = new ProjectTable();
        projectTable.setId(projectId);

        List<Integer> levels = (levelParam == null || levelParam.isEmpty())
                ? null : Arrays.stream(levelParam.split(","))
                .map(Integer::parseInt)
                .toList();

		return processModelDao.getProcessModelsWithChildren(projectTable)
				.stream()
				.filter(model -> levels == null || levels.contains(model.getLevel()))
				.map(model -> ProcessmodelMapper.map(model, relatedProcessModelDao, relatedProcessModelRepository))
				.toList();
    }

    @Override
    public ProcessInformation getProcessInformationById(Long projectId, Long id) {
        ProjectTable projectTable = new ProjectTable();
        projectTable.setId(projectId);
        ProcessModelTable model = processModelDao.getProcessModelById(projectTable, id);

		return ProcessmodelMapper.map(model, relatedProcessModelDao, relatedProcessModelRepository);
    }

    @Override
    public ProcessDetails getProcessDetails(Long id, boolean aggregate) {
        if (aggregate) {
            Set<ProcessModelTable> relatedProcessModels = processModelDao.findWithRelatedProcessModels(id);
            Set<ProcessEventTable> allEvents = processModelDao.findEventsForProcessModels(relatedProcessModels);
            Set<CallActivityTable> allCallActivities =
					processModelDao.findCallActivitiesForProcessModels(relatedProcessModels);
            return ProcessDetailsMapper.mapWithAggregation(id, relatedProcessModels, allEvents, allCallActivities);
        } else {
            ProcessModelTable table = processModelDao.findWithEventsAndActivities(id);
            return ProcessDetailsMapper.map(table);
        }
    }

	@Override
	public void saveMessageFlows(List<MessageFlowDetails> messageFlows, Long projectId) {
		ProjectTable projectTable = new ProjectTable();
		projectTable.setId(projectId);

        messageFlows.forEach(messageFlow ->
            messageFlowDao.persist(MessageFlowMapper.map(messageFlow, projectTable)));
    }

    private void connectDataStores(ProcessModelTable table, ProjectTable projectTable) {
        table//
                .getDataStores()//
                .forEach(store -> connectProcessDataStore(store, table, projectTable));

    }

	private void connectProcessDataStore(ProcessDataStoreTable store, ProcessModelTable table,
			ProjectTable projectTable) {
		DataStoreTable dataStoreTable;
		try {

			dataStoreTable = dataStoreDao.getDataStoreForLabel(store.getLabel(), projectTable);
		} catch (NoResultException e) {
			dataStoreTable = new DataStoreTable();
			dataStoreTable.setLabel(store.getLabel());
			dataStoreTable.setProject(projectTable);
			dataStoreDao.persist(dataStoreTable);
		}

		DataStoreConnectionTable connectionTable = new DataStoreConnectionTable();
		connectionTable.setAccess(store.getAccess());
		connectionTable.setProcess(table);
		connectionTable.setDataStore(dataStoreTable);
		connectionTable.setProject(projectTable);

		dataStoreConnectionDao.persist(connectionTable);
	}

    private void connectCallActivities(ProcessModel processModel, ProcessModelTable table, ProjectTable projectTable) {
        processModel//
                .getCallActivities()//
                .forEach(activity ->
                    connectCallActivityWithProcess(table, activity, projectTable));

		connectProcessWithCallActivity(table, projectTable);
	}

	private void connectProcessWithCallActivity(ProcessModelTable table, ProjectTable projectTable) {
		List<CallActivityTable> callActivityTables = callActivityDao.getCallActivitiesForName(table.getName(),
				projectTable);

		callActivityTables.forEach(callActivityTable -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(callActivityTable.getProcessModel());
			connection.setCallingElementType(ProcessElementType.CALL_ACTIVITY);
			connection.setCallingElement(callActivityTable.getElementId());

			connection.setCalledProcess(table);
			connection.setCalledElementType(ProcessElementType.START_EVENT);
			// called element remains empty
			connection.setLabel(callActivityTable.getLabel());
			connection.setProject(projectTable);
			connection.setUserCreated(false);
			processConnectionDao.persist(connection);
		});
	}

	private void connectCallActivityWithProcess(ProcessModelTable table, ProcessActivity activity,
			ProjectTable projectTable) {
		List<ProcessModelTable> processModelTable = processModelDao.getProcessModelsForName(activity.getLabel(),
				projectTable);

		processModelTable.forEach(process -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(table);
			connection.setCallingElement(activity.getElementId());
			connection.setCallingElementType(ProcessElementType.CALL_ACTIVITY);
			connection.setCalledProcess(process);
			// When the entire process is called, the called process is started using the
			// start event
			connection.setCalledElementType(ProcessElementType.START_EVENT);
			// called element remains empty
			connection.setLabel(process.getName());
			connection.setProject(projectTable);
			connection.setUserCreated(false);
			processConnectionDao.persist(connection);
		});
	}

    private void connectEvents(ProcessModel processModel, ProcessModelTable table, ProjectTable projectTable) {
        processModel//
                .getEvents()//
                .forEach(event -> connectEvents(table, event, projectTable));
    }

    private void connectEvents(ProcessModelTable table, ProcessEvent event, ProjectTable projectTable) {
        if (event.getEventType() == EventType.START || event.getEventType() == EventType.INTERMEDIATE_CATCH) {
            connectWithThrowEvents(table, event, EventType.INTERMEDIATE_THROW, projectTable);
            connectWithThrowEvents(table, event, EventType.END, projectTable);
        } else if (event.getEventType() == EventType.INTERMEDIATE_THROW || event.getEventType() == EventType.END) {
            connectWithCatchEvents(table, event, EventType.START, projectTable);
            connectWithCatchEvents(table, event, EventType.INTERMEDIATE_CATCH, projectTable);
        } else {
            throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
        }
    }

	private void connectWithCatchEvents(ProcessModelTable newTable, ProcessEvent newThrowEvent,
			EventType eventTypeToConnectTo, ProjectTable projectTable) {
		List<ProcessEventTable> startEventsWithSameLabel = processEventDao
				.getEventsForLabelAndType(newThrowEvent.getLabel(), eventTypeToConnectTo, projectTable);

		startEventsWithSameLabel.forEach(event -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(newTable);
			connection.setCallingElement(newThrowEvent.getElementId());
			if (newThrowEvent.getEventType().equals(EventType.INTERMEDIATE_THROW)) {
				connection.setCallingElementType(ProcessElementType.INTERMEDIATE_THROW_EVENT);
			} else {
				connection.setCallingElementType(ProcessElementType.END_EVENT);
			}

			connection.setCalledProcess(event.getProcessModel());
			connection.setCalledElement(event.getElementId());
			if (eventTypeToConnectTo.equals(EventType.START)) {
				connection.setCalledElementType(ProcessElementType.START_EVENT);
			} else {
				connection.setCalledElementType(ProcessElementType.INTERMEDIATE_CATCH_EVENT);
			}

			connection.setLabel(event.getLabel());
			connection.setProject(projectTable);
			connection.setUserCreated(false);
			processConnectionDao.persist(connection);
		});
	}

	private void connectWithThrowEvents(ProcessModelTable newTable, ProcessEvent newEvent,
			EventType eventTypeForConnectionFrom, ProjectTable projectTable) {
		List<ProcessEventTable> endEventsWithSameLabel = processEventDao.getEventsForLabelAndType(newEvent.getLabel(),
				eventTypeForConnectionFrom, projectTable);

		endEventsWithSameLabel.forEach(event -> {
			ProcessConnectionTable connection = new ProcessConnectionTable();
			connection.setCallingProcess(event.getProcessModel());
			connection.setCallingElement(event.getElementId());
			if (eventTypeForConnectionFrom.equals(EventType.END)) {
				connection.setCallingElementType(ProcessElementType.END_EVENT);
			} else {
				connection.setCallingElementType(ProcessElementType.INTERMEDIATE_THROW_EVENT);
			}

			connection.setCalledProcess(newTable);
			connection.setCalledElement(newEvent.getElementId());
			if (newEvent.getEventType().equals(EventType.START)) {
				connection.setCalledElementType(ProcessElementType.START_EVENT);
			} else {
				connection.setCalledElementType(ProcessElementType.INTERMEDIATE_CATCH_EVENT);
			}

			connection.setLabel(event.getLabel());
			connection.setProject(projectTable);
			connection.setUserCreated(false);
			processConnectionDao.persist(connection);
		});
	}

	@Override
	public ProcessModelTable getProcessModel(Long id) {
		return processModelDao.find(id);
	}

	@Override
	public String getProcessModelXml(Long id) {
		byte[] xmlBytes = processModelDao.getBpmnXml(id);
		return XmlConverter.bytesToString(xmlBytes);
	}

	@Override
	public void deleteProcessModel(Long id) {
        ProcessModelTable processModel = processModelDao.find(id);
        ProjectTable project = processModel.getProject();

		List<Long> relatedProcessModelIdsToDelete = getRelatedProcessModelsToDelete(id, new ArrayList<>());
		relatedProcessModelIdsToDelete.forEach(processModelId -> {
			dataStoreConnectionDao.deleteForProcessModel(processModelId);
			processConnectionDao.deleteForProcessModel(processModelId);
			messageFlowDao.deleteForProcessModel(processModelId);
            relatedProcessModelDao.deleteByRelatedProcessModelId(processModelId);
		});

		processModelDao.delete(relatedProcessModelIdsToDelete);
        relatedProcessModelRepository.calculateAndSaveRelatedProcessModels(project);
	}

	private List<Long> getRelatedProcessModelsToDelete(Long id, List<Long> processModelIdsToDelete) {
		if (processModelIdsToDelete.contains(id)) {
			return processModelIdsToDelete;
		}

		processModelIdsToDelete.add(id);
		ProcessModelTable processModelWithParentsAndChildren = processModelDao.findWithParentsAndChildren(id);
		Set<ProcessModelTable> children = processModelWithParentsAndChildren.getChildren();
		Set<ProcessModelTable> parents = processModelWithParentsAndChildren.getParents();

		for (ProcessModelTable child : children) {
			ProcessModelTable childWithParents = processModelDao.findWithParents(child.getId());
			boolean hasOtherParent = childWithParents.getParents().size() > 1;
			if (!hasOtherParent) {
				processModelIdsToDelete = getRelatedProcessModelsToDelete(child.getId(), processModelIdsToDelete);
			}
		}

		for (ProcessModelTable parent : parents) {
			processModelIdsToDelete = getRelatedProcessModelsToDelete(parent.getId(), processModelIdsToDelete);
		}

		return processModelIdsToDelete;
	}

	@Override
	public ProcessModelTable findByNameOrBpmnProcessIdWithoutCollaborations(String name, String bpmnProcessId,
			Long projectId) {
		ProjectTable project = new ProjectTable();
		project.setId(projectId);
		return processModelDao.findByNameOrBpmnProcessIdWithoutCollaborations(name, bpmnProcessId, project);
	}

	@Override
	public void handleProcessChangeAnalysis(Long oldProcessId, String newContent) {
		if (settingsRepository.getSettings() == null || settingsRepository.getSettings().getGeminiApiKey() == null) {
			throw new IllegalArgumentException("Settings not defined. Aborting process change analysis.");
		}

		String geminiApiKey = settingsRepository.getSettings().getGeminiApiKey();

		if (!geminiApiKey.startsWith("AIza") || geminiApiKey.length() != 39) {
			throw new IllegalArgumentException("Invalid Gemini API key. Aborting process change analysis.");
		}

		ProcessModelTable processModel = processModelDao.find(oldProcessId);
		List<RelatedProcessModelTable> relatedProcessModels =
				relatedProcessModelDao.getRelatedProcessModels(processModel);

		if (!relatedProcessModels.isEmpty()) {
			for (RelatedProcessModelTable relatedProcessModel : relatedProcessModels) {
				try {
					String geminiRequest =
							generateGeminiRequest(oldProcessId, newContent, relatedProcessModel);
					String geminiResponse = sendGeminiRequest(geminiApiKey, geminiRequest);
					handleGeminiResponse(geminiResponse, relatedProcessModel.getRelatedProcessModelId());
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
			}
		}
	}

	private String generateGeminiRequest(Long processModelId, String newContent,
										 RelatedProcessModelTable relatedProcessModel) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> requestBodyMap = new HashMap<>();
		List<Map<String, Object>> contentsList = new ArrayList<>();
		Map<String, Object> contentMap = new HashMap<>();
		List<Map<String, Object>> partsList = new ArrayList<>();

		String prompt =
				"Process model with id " + processModelId + ":\n\n" + getProcessModelXml(processModelId) + "\n\n" +
				"Related process model with id " + relatedProcessModel.getRelatedProcessModelId() + ":\n" +
				getProcessModelXml(relatedProcessModel.getRelatedProcessModelId()) + "\n\n" +
				"Changed process model with id " + processModelId + ":\n\n" + newContent + "\n\n" +
				"This is the context:" + "\n" +
				"There has been a change in process model with id " + processModelId + ". " +
				"Please analyse this change and what the differences are. " +
				"Process model with id " + relatedProcessModel.getRelatedProcessModelId() +
				" is the same process, but at a different level of abstraction. " +
				"Therefore, there should also be a change in process model with id " +
				relatedProcessModel.getRelatedProcessModelId() + " based on the change in process model with id " +
				processModelId + ". " + "Please give me the customised XML file of process model with id " +
				relatedProcessModel.getRelatedProcessModelId() + ". " +
				"The output must be a valid BPMN XML file according to OMG. " +
				"Please make sure that only the valid XML code is returned. " +
				"I do not need an explanation. If no change is necessary, simply return 'No'. " +
				"If no automatic change is possible or the changed process does not reflect the original process, " +
				"please return your analysis.";

		Map<String, Object> partMap = new HashMap<>();
		partMap.put(TEXT, prompt);
		partsList.add(partMap);
		contentMap.put(PARTS, partsList);
		contentsList.add(contentMap);
		requestBodyMap.put(CONTENTS, contentsList);

		return StringEscapeUtils.unescapeJavaScript(objectMapper.writeValueAsString(requestBodyMap));
	}

	private String sendGeminiRequest(String geminiApiKey, String requestBody) throws JsonProcessingException {
		String geminiModel = "gemini-2.0-flash-thinking-exp-01-21";

		ObjectMapper mapper = new ObjectMapper();
		Client client = ClientBuilder.newClient();

		ObjectNode payload = mapper.createObjectNode();
		ArrayNode contentsArray = mapper.createArrayNode();
		ObjectNode contentObject = mapper.createObjectNode();
		ArrayNode partsArray = mapper.createArrayNode();
		ObjectNode textObject = mapper.createObjectNode();

		textObject.put(TEXT, requestBody);
		partsArray.add(textObject);
		contentObject.set(PARTS, partsArray);
		contentsArray.add(contentObject);
		payload.set(CONTENTS, contentsArray);

		try (Response response = client.target("https://generativelanguage.googleapis.com/v1beta/models/" +
						geminiModel + ":generateContent?key=" + geminiApiKey)
						.request(MediaType.APPLICATION_JSON)
						.post(Entity.json(mapper.writeValueAsString(payload)))) {
			return response.readEntity(String.class);
		} finally {
			client.close();
		}

	}

	private void handleGeminiResponse(String geminiResponse, Long relatedProcessModelId) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(geminiResponse);
		JsonNode textNode = rootNode
				.path("candidates").path(0)
				.path("content")
				.path(PARTS).path(0)
				.path(TEXT);
		String responseText = textNode.asText();

		if (responseText.startsWith("```xml")) {
			String cleanedBpmnXml = cleanupBpmnXml(responseText);

			ProcessModelTable relatedProcessModel = processModelDao.find(relatedProcessModelId);
			if (relatedProcessModel != null) {
				relatedProcessModel.setBpmnXml(XmlConverter.stringToBytes(cleanedBpmnXml));
				processModelDao.merge(relatedProcessModel);
				logger.info("Process model updated.");
			} else {
				logger.info("Process model not updated because relatedProcessModel is null.");
			}
		} else if (responseText.toLowerCase().startsWith("no")) {
			logger.info("No change necessary.");
		} else {
			logger.info("Manual analysis necessary.");
		}
	}

	private String cleanupBpmnXml(String responseText) throws Exception {
		String xmlContent = responseText
				.replace("```xml", "") // remove the starting marker
				.replace("```", "") // remove the closing marker
				.trim(); // remove any extra leading/trailing whitespace

		// Unescape Java string literals to convert \n into actual newlines, \" into quotes, etc.
		xmlContent = StringEscapeUtils.unescapeJava(xmlContent);
		return xmlContent;
	}
}
