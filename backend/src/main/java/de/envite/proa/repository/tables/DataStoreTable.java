package de.envite.proa.repository.tables;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class DataStoreTable {

	@Id
    @GeneratedValue
    private Long id;
    
	@Column(unique=true)
    private String label;
	
    @ManyToOne
    private ProjectTable project;
}