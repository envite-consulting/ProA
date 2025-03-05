package de.envite.proa.repository.tables;

import de.envite.proa.entities.process.ProcessElementType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class MessageFlowTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String bpmnId;

	private String name;

	private String description;

	@ManyToOne(fetch = FetchType.EAGER)
	private ProcessModelTable callingProcess;

	@ManyToOne(fetch = FetchType.EAGER)
	private ProcessModelTable calledProcess;

	@Enumerated(EnumType.STRING)
	private ProcessElementType callingElementType;

	@Enumerated(EnumType.STRING)
	private ProcessElementType calledElementType;

	@ManyToOne(fetch = FetchType.LAZY)
	private ProjectTable project;
}