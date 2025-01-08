package de.envite.proa.entities.process;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessConnection {
	private Long id;

	private Long callingProcessid;
	private ProcessElementType callingElementType;
	
	private Long calledProcessid;
	private ProcessElementType calledElementType;
	
	private String label;
	private Boolean userCreated;
}