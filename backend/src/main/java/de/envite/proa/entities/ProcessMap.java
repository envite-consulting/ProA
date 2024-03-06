package de.envite.proa.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ProcessMap {

	private List<ProcessInformation> processes = new ArrayList<>();
	private List<ProcessConnection> connections = new ArrayList<>();
}
