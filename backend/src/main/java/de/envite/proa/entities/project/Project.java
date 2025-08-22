package de.envite.proa.entities.project;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class Project {

	private Long id;
	private String name;
	private Set<ProjectVersion> versions = new HashSet<>();
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
	private Set<ProjectMember> projectMembers = new HashSet<>();
}