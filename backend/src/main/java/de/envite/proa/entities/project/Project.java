package de.envite.proa.entities.project;

import de.envite.proa.entities.authentication.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class Project {

	private Long id;
	private String name;
	private Set<ProjectVersion> versions = new HashSet<>();
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
	private User owner;
	private Set<User> contributors = new HashSet<>();
}