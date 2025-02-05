package de.envite.proa.repository.tables;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class ProjectTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;

	private String name;
	private String version;
	private LocalDateTime createdAt;
	private LocalDateTime modifiedAt;

	@ManyToOne
	private UserTable user;
}