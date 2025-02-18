package de.envite.proa.repository.tables;

import de.envite.proa.entities.process.ProcessType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class ProcessModelTable {

	public ProcessModelTable() {
	}

	public ProcessModelTable(Long id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	private String name;

	private String bpmnProcessId;

	@Column(columnDefinition = "BYTEA")
	private byte[] bpmnXml;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModel")
	private List<ProcessEventTable> events = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModel")
	private List<CallActivityTable> callActivites = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModel")
	private List<ProcessDataStoreTable> dataStores = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModel")
	private List<RelatedProcessModelTable> relatedProcessModels = new ArrayList<>();

	@Lob
	@Column
	private String description;

	@Column
	private LocalDateTime createdAt;

	@Column
	private Integer level;

	@ManyToOne(fetch = FetchType.LAZY)
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

	private ProcessType processType;
}
