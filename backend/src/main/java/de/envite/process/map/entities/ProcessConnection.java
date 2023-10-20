package de.envite.process.map.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessConnection {

	private Long callingProcessid;
	private Long calledProcessid;
}
