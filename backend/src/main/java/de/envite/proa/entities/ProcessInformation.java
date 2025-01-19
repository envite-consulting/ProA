package de.envite.proa.entities;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInformation {
	
	private Long id;
	private String bpmnProcessId;
	private String processName;
	private String description; 
    private LocalDateTime createdAt;
	private Integer level;
	private List<String> parentsBpmnProcessIds;
	private List<String> childrenBpmnProcessIds;
	private List<ProcessLevel> processLevels;
}
