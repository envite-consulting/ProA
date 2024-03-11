package de.envite.proa.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDataStore {
	
	private String elementId;
	private String label;
	private DataAccess access;
}