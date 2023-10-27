package de.envite.process.map.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessModel {
	
    private String name;
    
    private String bpmnXml;
    
    private List<ProcessEvent> startEvents = new ArrayList<>();
    
    private List<ProcessEvent> intermediateEvents = new ArrayList<>();
    
    private List<ProcessEvent> endEvents = new ArrayList<>();
    
    private String description;
}