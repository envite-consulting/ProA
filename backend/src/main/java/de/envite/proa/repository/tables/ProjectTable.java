package de.envite.proa.repository.tables;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
public class ProjectTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProjectVersionTable> versions = new HashSet<>();

	@OneToMany(fetch = FetchType.LAZY)
	private Set<UserTable> contributors = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	private UserTable owner;
}