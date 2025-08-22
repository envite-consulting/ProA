package de.envite.proa.entities.project;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectMember {
	
    private String firstName;
    private String lastName;
    private ProjectRole role;

}
