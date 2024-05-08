package de.envite.proa.entities;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Project {

	private Long id;
	private String name;
	private String version;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;
}