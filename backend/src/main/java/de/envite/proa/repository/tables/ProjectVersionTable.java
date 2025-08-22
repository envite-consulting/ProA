package de.envite.proa.repository.tables;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ProjectVersionTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	private String name;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
	
	@ManyToOne
	private ProjectTable project;

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

	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "project")
	private List<ProcessModelTable> processModels = new ArrayList<>();

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectVersionTable other = (ProjectVersionTable) obj;
		return Objects.equals(id, other.id);
	}
}