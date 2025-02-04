package de.envite.proa.entities.process;

import de.envite.proa.entities.datastore.DataAccess;
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