package de.envite.proa.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessConnection {

	private Long callingProcessid;
	private ProcessElementType callingElementType;
	
	private Long calledProcessid;
	private ProcessElementType calledElementType;
}
