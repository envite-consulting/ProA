package de.envite.proa.entities.project;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectVersion {

	private Long id;
	private String name;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}
