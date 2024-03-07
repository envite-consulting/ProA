package de.envite.proa.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessModel {
	
    private String name;
    
    private String bpmnXml;
    
    private List<ProcessEvent> events = new ArrayList<>();
    
    private List<ProcessActivity> callActivities = new ArrayList<>();
    
    private String description;
}