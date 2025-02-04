package de.envite.proa.repository.messageflow;

import de.envite.proa.entities.collaboration.MessageFlowDetails;
import de.envite.proa.repository.processmodel.ProcessModelDao;
import de.envite.proa.repository.tables.MessageFlowTable;
import de.envite.proa.repository.tables.ProjectTable;

public class MessageFlowMapper {

	static public MessageFlowTable map(MessageFlowDetails messageFlowDetails, ProjectTable project,
			ProcessModelDao processModelDao) {
		MessageFlowTable table = new MessageFlowTable();
		table.setBpmnId(messageFlowDetails.getBpmnId());
		table.setName(messageFlowDetails.getName());
		table.setDescription(messageFlowDetails.getDescription());
		table.setCallingProcess(processModelDao.find(messageFlowDetails.getCallingProcessId()));
		table.setCalledProcess(processModelDao.find(messageFlowDetails.getCalledProcessId()));
		table.setCallingElementType(messageFlowDetails.getCallingElementType());
		table.setCalledElementType(messageFlowDetails.getCalledElementType());
		table.setProject(project);
		return table;
	}

	static public MessageFlowDetails map(MessageFlowTable table) {
		MessageFlowDetails details = new MessageFlowDetails();
		details.setBpmnId(table.getBpmnId());
		details.setName(table.getName());
		details.setDescription(table.getDescription());
		details.setCallingProcessId(table.getCallingProcess().getId());
		details.setCalledProcessId(table.getCalledProcess().getId());
		details.setCallingElementType(table.getCallingElementType());
		details.setCalledElementType(table.getCalledElementType());
		return details;
	}
}
