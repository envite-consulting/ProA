package de.envite.proa.repository.tables;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProcessModelTable {

	@Id
	@GeneratedValue
	public Long id;

	private String name;

	private String bpmnProcessId;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String bpmnXml;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModel")
	private List<ProcessEventTable> events = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModel")
	private List<CallActivityTable> callActivites = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModel")
	private List<ProcessDataStoreTable> dataStores = new ArrayList<>();

	@Lob
	@Column
	private String description;

	@Column
	private LocalDateTime createdAt;

	@ManyToOne
	private ProjectTable project;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable( //
			name = "processmodelrelations", //
			joinColumns = @JoinColumn(name = "parent_id"), //
			inverseJoinColumns = @JoinColumn(name = "child_id") //
	)
	private List<ProcessModelTable> children = new ArrayList<>();

	@ManyToMany(mappedBy = "children", fetch = FetchType.EAGER)
	private List<ProcessModelTable> parents = new ArrayList<>();
}