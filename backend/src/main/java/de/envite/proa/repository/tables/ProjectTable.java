package de.envite.proa.repository.tables;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NamedEntityGraph(
		name = "Project.withVersions",
		attributeNodes = @NamedAttributeNode("versions")
)

@NamedEntityGraph(
		name = "Project.withContributors",
		attributeNodes = @NamedAttributeNode("userRelations")
)

@NamedEntityGraph(
		name = "Project.withVersionsAndContributors",
		attributeNodes = {
				@NamedAttributeNode("versions"),
				@NamedAttributeNode("userRelations"),
		}
)

@Getter
@Setter
@Entity
public class ProjectTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String name;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "project")
	private Set<ProjectVersionTable> versions = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "project")
	private Set<ProjectUserRelationTable> userRelations = new HashSet<>();
}