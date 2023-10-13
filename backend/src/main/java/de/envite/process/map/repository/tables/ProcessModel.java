package de.envite.process.map.repository.tables;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class ProcessModel {
	
    @Id
    @GeneratedValue
    public Long id;
    
    private String name;
    
    @Lob
    @Column
    private String bpmnXml;
    
    @OneToMany
    private List<ProceessEvent> startEvents = new ArrayList<>();
    
    @OneToMany
    private List<ProceessEvent> intermediateEvents = new ArrayList<>();
    
    @OneToMany
    private List<ProceessEvent> endEvents = new ArrayList<>();
}