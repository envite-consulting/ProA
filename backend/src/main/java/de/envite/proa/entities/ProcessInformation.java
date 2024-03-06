package de.envite.proa.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInformation {
	
	private Long id;
	private String processName;
	private String description; 
    private LocalDateTime createdAt;
}