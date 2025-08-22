package de.envite.proa.repository.tables;

import de.envite.proa.entities.project.ProjectRole;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ProjectUserRelationTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	private UserTable user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private ProjectTable project;
	
	@Enumerated(EnumType.STRING)
	private ProjectRole role;
}
