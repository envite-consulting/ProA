package de.envite.proa.repository.tables;

import de.envite.proa.entities.process.ProcessElementType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProcessConnectionTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * the name of the common elements, e.g. the name of the connecting start and end event
	 */
	private String label;

	@ManyToOne(fetch = FetchType.EAGER)
	private ProcessModelTable callingProcess;

	@ManyToOne(fetch = FetchType.EAGER)
	private ProcessModelTable calledProcess;

	@Enumerated(EnumType.STRING)
	private ProcessElementType callingElementType;

	@Enumerated(EnumType.STRING)
	private ProcessElementType calledElementType;

	private String callingElement;

	private String calledElement;

	@ManyToOne
	private ProjectTable project;

	private Boolean userCreated;
}