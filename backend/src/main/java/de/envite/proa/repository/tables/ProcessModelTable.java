package de.envite.proa.repository.tables;

import de.envite.proa.entities.process.ProcessType;
import de.envite.proa.util.SearchLabelBuilder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NamedEntityGraph(
		name = "ProcessModel.withChildren",
		attributeNodes = @NamedAttributeNode("children")
)

@NamedEntityGraph(
		name = "ProcessModel.withParents",
		attributeNodes = @NamedAttributeNode("parents")
)

@NamedEntityGraph(
		name = "ProcessModel.withChildrenAndParents",
		attributeNodes = {
				@NamedAttributeNode("children"),
				@NamedAttributeNode("parents"),
		}
)

@NamedEntityGraph(
		name = "ProcessModel.withEventsAndActivities",
		attributeNodes = {
				@NamedAttributeNode("events"),
				@NamedAttributeNode("callActivites"),
		}
)

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProcessModelTable {

	public ProcessModelTable() {
	}

	public ProcessModelTable(Long id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@EqualsAndHashCode.Include
	public Long id;

	@EqualsAndHashCode.Include
	private String name;
	
    private String searchLabel;

	@EqualsAndHashCode.Include
	private String bpmnProcessId;

	@Column(columnDefinition = "BYTEA")
	@Basic(fetch = FetchType.LAZY)
	private byte[] bpmnXml;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModel", fetch = FetchType.LAZY)
	private Set<ProcessEventTable> events = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModel", fetch = FetchType.LAZY)
	private Set<CallActivityTable> callActivites = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "processModel", fetch = FetchType.LAZY)
	private List<ProcessDataStoreTable> dataStores = new ArrayList<>();

	@Lob
	@Column
	@EqualsAndHashCode.Include
	private String description;
	
	@Column
	@EqualsAndHashCode.Include
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@EqualsAndHashCode.Include
	private ProjectVersionTable project;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable( //
			name = "processmodelrelations", //
			joinColumns = @JoinColumn(name = "parent_id"), //
			inverseJoinColumns = @JoinColumn(name = "child_id") //
	)
	private Set<ProcessModelTable> children = new HashSet<>();

	@ManyToMany(mappedBy = "children", fetch = FetchType.LAZY)
	private Set<ProcessModelTable> parents = new HashSet<>();

	@EqualsAndHashCode.Include
	private ProcessType processType;
	
    @PrePersist
    @PreUpdate
    private void generateSearchLabel() {
        this.searchLabel = SearchLabelBuilder.buildSearchLabel(this.name);
    }
}