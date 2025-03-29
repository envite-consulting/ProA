package de.envite.proa.repository.tables;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NamedEntityGraph(
		name = "Project.withVersions",
		attributeNodes = @NamedAttributeNode("versions")
)

@NamedEntityGraph(
		name = "Project.withContributors",
		attributeNodes = @NamedAttributeNode("contributors")
)

@NamedEntityGraph(
		name = "Project.withVersionsAndContributors",
		attributeNodes = {
				@NamedAttributeNode("versions"),
				@NamedAttributeNode("contributors"),
		}
)

@Data
@Entity
public class ProjectTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private Set<ProjectVersionTable> versions = new HashSet<>();

	@OneToMany(fetch = FetchType.LAZY)
	private Set<UserTable> contributors = new HashSet<>();

	@ManyToOne
	private UserTable owner;
}