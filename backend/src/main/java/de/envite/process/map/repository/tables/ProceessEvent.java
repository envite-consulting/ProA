package de.envite.process.map.repository.tables;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ProceessEvent {

    @Id
    @GeneratedValue
    private Long id;
    
    private String elementId;
    
    private String label;
}
