package de.envite.proa.repository.tables;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DataStoreTable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String label;

	@ManyToOne
	private ProjectTable project;
}