package de.envite.proa.repository.tables;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class ProjectTable {

	@Id
	@GeneratedValue
	public Long id;

	private String name;
	private String version;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	@ManyToOne
	private UserTable user;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "project")
	private List<ProcessModelTable> processModels;
}
