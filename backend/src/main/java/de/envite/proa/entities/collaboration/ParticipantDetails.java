package de.envite.proa.entities.collaboration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDetails {

	private String name;
	private String description;
	private String xml;
}