package de.envite.process.map.repository.tables;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class ProcessModelTable {

	@Id
	@GeneratedValue
	public Long id;

	private String name;

	@Lob
	@Column
	private String bpmnXml;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModelForStartEvent")
	private List<ProcessEventTable> startEvents = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModelForIntermediateEvent")
	private List<ProcessEventTable> intermediateEvents = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModelForEndEvent")
	private List<ProcessEventTable> endEvents = new ArrayList<>();

	@Column
	private String description;
	
	@Column
	private LocalDateTime createdAt;
}