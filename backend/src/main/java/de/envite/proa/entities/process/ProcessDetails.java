package de.envite.proa.entities.process;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessDetails {
	
	private Long id;
	
    private String name;

    private String bpmnProcessId;
    
    private List<ProcessEvent> startEvents = new ArrayList<>();
    
    private List<ProcessEvent> intermediateCatchEvents = new ArrayList<>();
    
    private List<ProcessEvent> intermediateThrowEvents = new ArrayList<>();
    
    private List<ProcessEvent> endEvents = new ArrayList<>();
    
    private List<ProcessActivity> activities = new ArrayList<>();
    
    private String description;
}