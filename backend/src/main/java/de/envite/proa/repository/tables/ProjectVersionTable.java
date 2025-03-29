package de.envite.proa.repository.tables;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class ProjectVersionTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	private String name;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project")
	private List<CallActivityTable> callActivities = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project")
	private List<DataStoreConnectionTable> dataStoreConnections = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project")
	private List<DataStoreTable> dataStores = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project")
	private List<MessageFlowTable> messageFlows = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project")
	private List<ProcessConnectionTable> processConnections = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project")
	private List<ProcessEventTable> processEvents = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project")
	private List<ProcessModelTable> processModels = new ArrayList<>();
}
