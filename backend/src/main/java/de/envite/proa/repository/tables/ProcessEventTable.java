package de.envite.proa.repository.tables;

import de.envite.proa.entities.EventType;
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
public class ProcessEventTable {

    @Id
    @GeneratedValue
    private Long id;
    
    private String elementId;
    
    private String label;
    
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private ProcessModelTable processModel;
    
    @ManyToOne
    private ProjectTable project;
}