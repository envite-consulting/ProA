package de.envite.proa.repository.tables;

import de.envite.proa.entities.DataAccess;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class DataStoreConnectionTable {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	private ProcessModelTable process;

	@ManyToOne(fetch = FetchType.EAGER)
	private DataStoreTable dataStore;

	@Enumerated(EnumType.STRING)
	private DataAccess access;
}