package de.envite.proa.repository.tables;

import de.envite.proa.entities.DataAccess;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ProcessDataStoreTable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String elementId;
    
    private String label;
    
    @Enumerated(EnumType.STRING)
    private DataAccess access;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private ProcessModelTable processModel;
}