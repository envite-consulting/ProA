package de.envite.proa.repository.tables;

import de.envite.proa.entities.DataAccess;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DataStoreConnectionTable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	private ProcessModelTable process;

	@ManyToOne(fetch = FetchType.EAGER)
	private DataStoreTable dataStore;

	@Enumerated(EnumType.STRING)
	private DataAccess access;
	
	@ManyToOne
	private ProjectTable project;
}