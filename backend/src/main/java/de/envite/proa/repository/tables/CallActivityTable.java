package de.envite.proa.repository.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class CallActivityTable {

	@Id
	@GeneratedValue
	public Long id;
	
    private String elementId;
    
    private String label;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private ProcessModelTable processModel;
    
    @ManyToOne
    private ProjectTable project;
}