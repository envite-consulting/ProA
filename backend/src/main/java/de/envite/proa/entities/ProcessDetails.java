package de.envite.proa.entities;

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
    
    private List<ProcessEvent> startEvents = new ArrayList<>();
    
    private List<ProcessEvent> intermediateEvents = new ArrayList<>();
    
    private List<ProcessEvent> endEvents = new ArrayList<>();
    
    private String description;

}