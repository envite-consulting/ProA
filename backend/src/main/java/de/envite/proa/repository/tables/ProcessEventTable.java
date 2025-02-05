package de.envite.proa.repository.tables;

import de.envite.proa.entities.EventType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProcessEventTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String elementId;

	private String label;

	@Enumerated(EnumType.STRING)
	private EventType eventType;

	@ManyToOne(fetch = FetchType.LAZY)
	private ProcessModelTable processModel;

	@ManyToOne
	private ProjectTable project;
}