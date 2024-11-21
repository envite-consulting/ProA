package de.envite.proa.repository.tables;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CallActivityTable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;
	
    private String elementId;
    
    private String label;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private ProcessModelTable processModel;
    
    @ManyToOne
    private ProjectTable project;
}