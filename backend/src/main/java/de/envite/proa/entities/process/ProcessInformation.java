package de.envite.proa.entities.process;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInformation {
	private Long id;
	private String bpmnProcessId;
	private String processName;
	private String description;
	private LocalDateTime createdAt;
	private List<Long> childrenIds;
	private ProcessType processType;
}